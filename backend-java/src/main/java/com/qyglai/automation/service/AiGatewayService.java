package com.qyglai.automation.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qyglai.automation.dto.BossChatRequest;
import com.qyglai.automation.dto.BossChatResponse;
import com.qyglai.automation.dto.ParseAndExtractResponse;
import com.qyglai.automation.entity.AiModelCallLogEntity;
import com.qyglai.automation.mapper.AiModelCallLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Java AI 能力统一网关，负责业务上下文组装、模型调用、降级和调用审计。
 */
@Service
public class AiGatewayService {

    private final AiModelCallLogMapper aiModelCallLogMapper;
    private final BossBusinessContextService bossBusinessContextService;
    private final JavaAiModelGateway javaAiModelGateway;
    private final ObjectMapper objectMapper;
    private final JavaFileParserService javaFileParserService;
    private final JavaDocumentExtractionService javaDocumentExtractionService;

    public AiGatewayService(AiModelCallLogMapper aiModelCallLogMapper,
                            BossBusinessContextService bossBusinessContextService,
                            JavaAiModelGateway javaAiModelGateway,
                            ObjectMapper objectMapper,
                            JavaFileParserService javaFileParserService,
                            JavaDocumentExtractionService javaDocumentExtractionService) {
        this.aiModelCallLogMapper = aiModelCallLogMapper;
        this.bossBusinessContextService = bossBusinessContextService;
        this.javaAiModelGateway = javaAiModelGateway;
        this.objectMapper = objectMapper;
        this.javaFileParserService = javaFileParserService;
        this.javaDocumentExtractionService = javaDocumentExtractionService;
    }

    /**
     * 查询真实经营数据并调用大模型组织回答；模型不可用时返回数据库规则摘要。
     *
     * @param request 老板问题
     * @return 老板助手回答
     */
    public BossChatResponse askBossAssistant(BossChatRequest request) {
        long start = System.currentTimeMillis();
        String businessType = classifyBossBusinessType(request == null ? "" : request.question());
        // 先由 Java 根据问题类型查询真实数据库，模型只能使用该上下文组织答案。
        Map<String, Object> context = bossBusinessContextService.buildContext(request);
        BossChatRequest enrichedRequest = new BossChatRequest(
                request.question(), request.timeRange(), request.userId(), context
        );
        String requestText = enrichedRequest.toString();
        BossChatResponse databaseResponse = bossBusinessContextService.fallbackResponse(context);
        // databaseResponse 同时作为模型不可用时的确定性回答，老板问答不会因外部服务中断。
        String answer = javaAiModelGateway.generateText("boss_query",
                "你是企业老板的经营助手。所有数字必须严格来自 Java 查询 MySQL 后提供的上下文，禁止编造或修改数字。"
                        + "请直接回答问题，说明关键数字、风险和下一步动作。",
                "问题：" + request.question() + "\n时间范围：" + request.timeRange() + "\n数据库上下文：" + json(context),
                databaseResponse.answer()
        );

        boolean success = "success".equals(javaAiModelGateway.status().lastCallStatus());
        recordAiCall("boss_query", businessType, null,
                success ? javaAiModelGateway.status().model() : "fallback-local-rule",
                "java-direct/boss", requestText, start, success,
                success ? null : javaAiModelGateway.status().lastFallbackReason());
        if (!success) {
            return databaseResponse;
        }
        return new BossChatResponse(databaseResponse.intent(), answer, databaseResponse.metrics(),
                databaseResponse.actions(), databaseResponse.sources());
    }

    /**
     * 使用 Java 解析文件并抽取业务字段。
     *
     * @param file 上传文件
     * @param scenario 抽取场景
     * @return 文件解析和字段抽取结果
     */
    public ParseAndExtractResponse parseAndExtractFile(MultipartFile file, String scenario) {
        long start = System.currentTimeMillis();
        String normalizedScenario = scenario == null || scenario.isBlank() ? "general" : scenario;
        String requestText = (file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename())
                + ":" + file.getSize();
        try {
            // 文件解析负责得到原文，结构化抽取负责把原文转换为可入库业务字段。
            var parsed = javaFileParserService.parse(file);
            var extraction = javaDocumentExtractionService.extract(parsed.rawText(), normalizedScenario);
            recordAiCall("file_parse_extract", extraction.scenario(), null, javaAiModelGateway.status().model(),
                    "java-direct/files/parse-and-extract", requestText, start, true, null);
            return new ParseAndExtractResponse(parsed, extraction);
        } catch (Exception ex) {
            recordAiCall("file_parse_extract", normalizedScenario, null, "java-ai-service",
                    "java-direct/files/parse-and-extract", requestText, start, false, ex.getMessage());
            throw new IllegalStateException("Java AI 文件解析失败: " + ex.getMessage(), ex);
        }
    }

    private String json(Map<String, Object> context) {
        try {
            return objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException ex) {
            return context.toString();
        }
    }

    private void recordAiCall(String scenario, String businessType, Long businessId, String modelName,
                              String promptTemplateCode, String requestText, long startMs,
                              boolean success, String errorMessage) {
        try {
            // 调用日志只保存摘要、哈希和统计信息，避免把完整敏感业务原文重复落库。
            AiModelCallLogEntity log = new AiModelCallLogEntity();
            log.setId(IdWorker.getId());
            log.setScenario(scenario);
            log.setBusinessType(businessType);
            log.setBusinessId(businessId);
            log.setModelName(modelName);
            log.setPromptTemplateCode(promptTemplateCode);
            log.setRequestTokens(estimateTokens(requestText));
            log.setResponseTokens(0);
            log.setCostAmount(BigDecimal.ZERO);
            log.setLatencyMs((int) Math.max(0, System.currentTimeMillis() - startMs));
            log.setSuccessFlag(success ? 1 : 0);
            log.setErrorMessage(truncate(errorMessage));
            log.setRequestHash(DigestUtils.md5DigestAsHex(
                    (requestText == null ? "" : requestText).getBytes(StandardCharsets.UTF_8)));
            log.setCreatedAt(LocalDateTime.now());
            aiModelCallLogMapper.insert(log);
        } catch (RuntimeException ignored) {
            // AI 调用日志失败不能影响主业务流程。
        }
    }

    private int estimateTokens(String text) {
        return text == null || text.isBlank() ? 0 : Math.max(1, text.length() / 2);
    }

    private String classifyBossBusinessType(String question) {
        String text = question == null ? "" : question;
        if (containsAny(text, "销售", "客户", "商机", "跟进", "成交", "线索")) return "sales";
        if (containsAny(text, "财务", "发票", "对账", "回款", "付款", "金额", "费用")) return "finance";
        if (containsAny(text, "客服", "工单", "投诉", "满意", "交付进度")) return "customer_service";
        if (containsAny(text, "合同", "法务", "风险条款", "审批", "违约")) return "contract";
        if (containsAny(text, "复核", "待办", "审批任务", "人工处理")) return "review";
        if (containsAny(text, "日报", "周报", "月报", "报表", "导出", "汇总")) return "report";
        return "overview";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }
}
