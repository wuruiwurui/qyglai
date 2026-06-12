package com.qyglai.automation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qyglai.automation.entity.ContractRecordEntity;
import com.qyglai.automation.entity.InvoiceRecordEntity;
import com.qyglai.automation.entity.WorkflowInstanceEntity;
import com.qyglai.automation.mapper.ContractRecordMapper;
import com.qyglai.automation.mapper.InvoiceRecordMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * 工作流 AI 节点执行服务。
 *
 * <p>模型只提供审核建议；只有真实模型明确给出高置信度通过结论时才自动流转，
 * 其余情况统一转人工审批，避免模型异常直接改变业务状态。</p>
 */
@Service
public class WorkflowAiNodeService {

    private static final double AUTO_APPROVE_CONFIDENCE = 0.85;

    private final JavaAiModelGateway modelGateway;
    private final AiCallLogService aiCallLogService;
    private final ContractRecordMapper contractMapper;
    private final InvoiceRecordMapper invoiceMapper;
    private final ObjectMapper objectMapper;

    public WorkflowAiNodeService(JavaAiModelGateway modelGateway,
                                 AiCallLogService aiCallLogService,
                                 ContractRecordMapper contractMapper,
                                 InvoiceRecordMapper invoiceMapper,
                                 ObjectMapper objectMapper) {
        this.modelGateway = modelGateway;
        this.aiCallLogService = aiCallLogService;
        this.contractMapper = contractMapper;
        this.invoiceMapper = invoiceMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 执行工作流 AI 复核并返回标准化结果。
     */
    public AiNodeResult review(WorkflowInstanceEntity instance, String nodeCode, String nodeName) {
        long start = System.currentTimeMillis();
        Map<String, Object> businessContext = businessContext(instance);
        String requestText = toJson(Map.of(
                "nodeCode", nodeCode,
                "nodeName", nodeName,
                "businessType", value(instance.getBusinessType()),
                "businessId", instance.getBusinessId() == null ? "" : instance.getBusinessId(),
                "variables", value(instance.getVariablesJson()),
                "businessData", businessContext
        ));
        JsonNode response = modelGateway.generateJson(
                "你是企业审批流程中的AI复核节点。只能依据提供的业务数据审核，不得编造。"
                        + "只输出合法JSON：{\"decision\":\"auto_pass或manual_review\",\"confidence\":0到1,"
                        + "\"summary\":\"审核结论\",\"risks\":[\"风险说明\"]}。"
                        + "存在数据缺失、金额异常、风险、无法确认或低置信度时必须返回manual_review。",
                requestText
        );

        boolean modelSuccess = response != null && "success".equals(modelGateway.status().lastCallStatus());
        String decision = modelSuccess ? response.path("decision").asText("manual_review") : "manual_review";
        double confidence = modelSuccess ? clamp(response.path("confidence").asDouble(0.0)) : 0.0;
        String summary = modelSuccess ? response.path("summary").asText("AI复核未给出明确结论")
                : "真实模型调用失败或返回格式无效，已转人工审批";
        List<String> risks = new ArrayList<>();
        if (modelSuccess && response.path("risks").isArray()) {
            response.path("risks").forEach(item -> {
                if (!item.asText("").isBlank()) risks.add(item.asText());
            });
        }
        boolean autoApproved = modelSuccess
                && "auto_pass".equalsIgnoreCase(decision)
                && confidence >= AUTO_APPROVE_CONFIDENCE
                && risks.isEmpty();
        String normalizedDecision = autoApproved ? "auto_pass" : "manual_review";
        String error = modelSuccess ? null : modelGateway.status().lastFallbackReason();

        AiNodeResult result = new AiNodeResult(normalizedDecision, confidence, summary, risks,
                autoApproved, modelSuccess, modelGateway.status().model(), error);
        aiCallLogService.record("workflow_ai_review", value(instance.getBusinessType()), instance.getBusinessId(),
                modelSuccess ? modelGateway.status().model() : "fallback-manual-review",
                "java-direct/workflow/ai-review", requestText, toJson(result), start, modelSuccess, error);
        return result;
    }

    private Map<String, Object> businessContext(WorkflowInstanceEntity instance) {
        Map<String, Object> context = new LinkedHashMap<>();
        String type = value(instance.getBusinessType()).toLowerCase();
        if (instance.getBusinessId() == null) return context;
        if (type.contains("contract")) {
            ContractRecordEntity contract = contractMapper.selectById(instance.getBusinessId());
            if (contract != null) context.put("contract", contract);
        } else if (type.contains("invoice")) {
            InvoiceRecordEntity invoice = invoiceMapper.selectById(instance.getBusinessId());
            if (invoice != null) context.put("invoice", invoice);
        }
        return context;
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return String.valueOf(value);
        }
    }

    /**
     * 工作流 AI 节点标准化执行结果。
     */
    public record AiNodeResult(
            String decision,
            double confidence,
            String summary,
            List<String> risks,
            boolean autoApproved,
            boolean modelSuccess,
            String modelName,
            String fallbackReason
    ) {
    }
}
