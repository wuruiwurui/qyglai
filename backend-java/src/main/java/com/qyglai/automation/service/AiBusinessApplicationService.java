package com.qyglai.automation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qyglai.automation.dto.BossChatRequest;
import com.qyglai.automation.dto.ReportGenerateRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * 业务场景大模型应用服务。
 *
 * <p>Java 负责确定性业务计算和默认结果，大模型负责分类、解释及自然语言生成。
 * 模型失败或输出无效时返回规则结果，并统一记录模型调用日志。</p>
 */
@Service
public class AiBusinessApplicationService {

    private final JavaAiModelGateway modelGateway;
    private final AiCallLogService callLogService;
    private final BossBusinessContextService bossBusinessContextService;
    private final ObjectMapper objectMapper;

    public AiBusinessApplicationService(JavaAiModelGateway modelGateway, AiCallLogService callLogService,
                                        BossBusinessContextService bossBusinessContextService, ObjectMapper objectMapper) {
        this.modelGateway = modelGateway;
        this.callLogService = callLogService;
        this.bossBusinessContextService = bossBusinessContextService;
        this.objectMapper = objectMapper;
    }

    public TicketDecision classifyTicket(String content) {
        TicketDecision fallback = new TicketDecision("其他咨询", "P2", "neutral", "客服主管",
                0.60, "您好，已收到您的问题，我们会尽快核实并反馈处理进展。", false);
        String input = value(content);
        JsonNode node = callJson("ticket_classify", "customer_service", null, "java-direct/tickets/classify",
                "你是企业客服工单分类助手。只能根据工单内容判断，只输出合法JSON："
                        + "{\"category\":\"分类\",\"priority\":\"P1/P2/P3\",\"sentiment\":\"positive/neutral/negative\","
                        + "\"suggestedOwner\":\"建议处理角色\",\"confidence\":0到1,\"replySuggestion\":\"建议回复\"}。",
                input);
        if (node == null) return fallback;
        return new TicketDecision(text(node, "category", fallback.category()),
                priority(text(node, "priority", fallback.priority())),
                sentiment(text(node, "sentiment", fallback.sentiment())),
                text(node, "suggestedOwner", fallback.suggestedOwner()),
                confidence(node, fallback.confidence()),
                text(node, "replySuggestion", fallback.replySuggestion()), true);
    }

    public ReconciliationDecision explainReconciliation(String supplierName, BigDecimal expected,
                                                        BigDecimal actual, BigDecimal difference) {
        String status = difference.signum() == 0 ? "matched" : "difference";
        ReconciliationDecision fallback = new ReconciliationDecision(status,
                difference.signum() == 0 ? "金额一致，可进入后续归档流程。" : "应有金额与实际金额存在差异，需要人工核对。",
                difference.signum() == 0 ? List.of() : List.of("核对发票号码", "确认付款流水", "联系供应商确认差异"),
                difference.signum() == 0 ? List.of() : List.of("对账金额存在差异"), 0.75, false);
        String input = toJson(Map.of("supplierName", value(supplierName), "expectedAmount", expected,
                "actualAmount", actual, "difference", difference, "javaCalculatedStatus", status));
        JsonNode node = callJson("reconciliation_analysis", "finance", null, "java-direct/reconciliation/analyze",
                "你是财务对账分析助手。金额和状态均由Java计算，不得修改。只输出合法JSON："
                        + "{\"summary\":\"差异解释\",\"risks\":[],\"suggestions\":[],\"confidence\":0到1}。",
                input);
        if (node == null) return fallback;
        return new ReconciliationDecision(status, text(node, "summary", fallback.summary()),
                list(node, "suggestions", fallback.suggestions()), list(node, "risks", fallback.risks()),
                confidence(node, fallback.confidence()), true);
    }

    public ReviewDecision adviseReview(String scenario, String content, String riskLevel) {
        boolean highRisk = "high".equalsIgnoreCase(riskLevel);
        ReviewDecision fallback = new ReviewDecision(highRisk ? "need_review" : "manual_confirm",
                highRisk ? List.of("业务风险等级较高") : List.of("需要人工确认业务字段"),
                List.of("核对金额", "核对主体", "确认审批链路", "记录处理结论"), highRisk ? 0.80 : 0.65, false);
        String input = toJson(Map.of("scenario", value(scenario), "content", value(content), "riskLevel", value(riskLevel)));
        JsonNode node = callJson("review_advice", value(scenario), null, "java-direct/review/advice",
                "你是企业人工复核助手。只提供复核建议，不得替代人工作最终决定。只输出合法JSON："
                        + "{\"decision\":\"need_review或manual_confirm\",\"reasons\":[],\"checklist\":[],\"confidence\":0到1}。",
                input);
        if (node == null) return fallback;
        return new ReviewDecision(text(node, "decision", fallback.decision()),
                list(node, "reasons", fallback.reasons()), list(node, "checklist", fallback.checklist()),
                confidence(node, fallback.confidence()), true);
    }

    public ReportContent generateReport(ReportGenerateRequest request) {
        Map<String, Object> context = bossBusinessContextService.buildContext(
                new BossChatRequest("生成企业经营" + request.reportType(), request.timeRange(), null, Map.of()));
        ReportContent fallback = new ReportContent(String.valueOf(context.getOrDefault("summary", "暂无可汇总数据。")),
                toJson(context), List.of("经营概览", "风险事项", "下一步动作"),
                List.of("企业业务数据库"), 0.70, false);
        String input = toJson(Map.of("reportType", request.reportType(), "timeRange", value(request.timeRange()),
                "audience", value(request.audience()), "databaseContext", context));
        JsonNode node = callJson("report_generate", "report", null, "java-direct/reports/generate",
                "你是企业经营报告生成助手。所有数字必须严格来自数据库上下文，不得编造。只输出合法JSON："
                        + "{\"summary\":\"摘要\",\"content\":\"完整正文\",\"sections\":[],\"sources\":[],\"confidence\":0到1}。",
                input);
        if (node == null) return fallback;
        return new ReportContent(text(node, "summary", fallback.summary()), text(node, "content", fallback.content()),
                list(node, "sections", fallback.sections()), list(node, "sources", fallback.sources()),
                confidence(node, fallback.confidence()), true);
    }

    private JsonNode callJson(String scenario, String businessType, Long businessId, String promptCode,
                              String systemPrompt, String input) {
        long start = System.currentTimeMillis();
        JsonNode node = modelGateway.generateJson(scenario, systemPrompt, input);
        boolean success = node != null && "success".equals(modelGateway.status().lastCallStatus());
        callLogService.record(scenario, businessType, businessId,
                success ? modelGateway.status().model() : "fallback-local-rule", promptCode,
                input, node == null ? "" : node.toString(), start, success,
                success ? null : modelGateway.status().lastFallbackReason());
        return success ? node : null;
    }

    private String text(JsonNode node, String field, String fallback) {
        String value = node.path(field).asText("");
        return value.isBlank() ? fallback : value;
    }

    private double confidence(JsonNode node, double fallback) {
        return Math.max(0.0, Math.min(1.0, node.path("confidence").asDouble(fallback)));
    }

    private List<String> list(JsonNode node, String field, List<String> fallback) {
        if (!node.path(field).isArray()) return fallback;
        List<String> result = new ArrayList<>();
        node.path(field).forEach(item -> {
            if (!item.asText("").isBlank()) result.add(item.asText());
        });
        return result.isEmpty() ? fallback : result;
    }

    private String priority(String value) {
        return List.of("P1", "P2", "P3").contains(value.toUpperCase()) ? value.toUpperCase() : "P2";
    }

    private String sentiment(String value) {
        return List.of("positive", "neutral", "negative").contains(value.toLowerCase()) ? value.toLowerCase() : "neutral";
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

    public record TicketDecision(String category, String priority, String sentiment, String suggestedOwner,
                                 double confidence, String replySuggestion, boolean modelSuccess) {}
    public record ReconciliationDecision(String status, String summary, List<String> suggestions,
                                         List<String> risks, double confidence, boolean modelSuccess) {}
    public record ReviewDecision(String decision, List<String> reasons, List<String> checklist,
                                 double confidence, boolean modelSuccess) {}
    public record ReportContent(String summary, String content, List<String> sections,
                                List<String> sources, double confidence, boolean modelSuccess) {}
}
