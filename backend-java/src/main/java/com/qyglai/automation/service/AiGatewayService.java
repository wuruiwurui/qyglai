package com.qyglai.automation.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.qyglai.automation.dto.BossChatRequest;
import com.qyglai.automation.dto.BossChatResponse;
import com.qyglai.automation.dto.ParseAndExtractResponse;
import com.qyglai.automation.entity.AiModelCallLogEntity;
import com.qyglai.automation.mapper.AiModelCallLogMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

/**
 * Java 后端访问 Python AI 服务的统一网关。
 *
 * <p>所有经过该网关的 AI 调用都会写入模型调用日志。日志失败不影响主业务流程。</p>
 */
@Service
public class AiGatewayService {

    private final RestClient aiRestClient;
    private final MockInsightService mockInsightService;
    private final AiModelCallLogMapper aiModelCallLogMapper;

    public AiGatewayService(RestClient aiRestClient, MockInsightService mockInsightService,
                            AiModelCallLogMapper aiModelCallLogMapper) {
        this.aiRestClient = aiRestClient;
        this.mockInsightService = mockInsightService;
        this.aiModelCallLogMapper = aiModelCallLogMapper;
    }

    /**
     * 调用老板经营助手，失败时自动降级到 Java 本地摘要。
     *
     * @param request 老板问题
     * @return 老板助手回答
     */
    public BossChatResponse askBossAssistant(BossChatRequest request) {
        long start = System.currentTimeMillis();
        String requestText = request == null ? "" : request.toString();
        String businessType = classifyBossBusinessType(request == null ? "" : request.question());
        try {
            BossChatResponse response = aiRestClient.post()
                    .uri("/api/v1/boss/query")
                    .body(request)
                    .retrieve()
                    .body(BossChatResponse.class);
            recordAiCall("boss_query", response == null || response.intent() == null ? businessType : response.intent(), null, "python-ai-service", "boss/query",
                    requestText, start, true, null);
            return response != null ? response : mockInsightService.answer(request);
        } catch (RuntimeException ex) {
            recordAiCall("boss_query", businessType, null, "fallback-local-rule", "boss/query",
                    requestText, start, false, "已降级到本地经营摘要：" + ex.getMessage());
            return mockInsightService.answer(request);
        }
    }

    /**
     * 调用 Python AI 服务解析文件并抽取业务字段。
     *
     * @param file 上传文件
     * @param scenario 抽取场景
     * @return 文件解析和抽取结果
     */
    public ParseAndExtractResponse parseAndExtractFile(MultipartFile file, String scenario) {
        long start = System.currentTimeMillis();
        String normalizedScenario = scenario == null || scenario.isBlank() ? "general" : scenario;
        String requestText = (file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename()) + ":" + file.getSize();
        try {
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);
            body.add("scenario", normalizedScenario);
            ParseAndExtractResponse response = aiRestClient.post()
                    .uri("/api/v1/files/parse-and-extract")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(ParseAndExtractResponse.class);
            recordAiCall("file_parse_extract", normalizedScenario, null, "python-ai-service",
                    "files/parse-and-extract", requestText, start, true, null);
            return response;
        } catch (Exception ex) {
            recordAiCall("file_parse_extract", normalizedScenario, null, "python-ai-service",
                    "files/parse-and-extract", requestText, start, false, ex.getMessage());
            throw new IllegalStateException("AI文件解析失败：" + ex.getMessage(), ex);
        }
    }

    private void recordAiCall(String scenario, String businessType, Long businessId, String modelName,
                              String promptTemplateCode, String requestText, long startMs,
                              boolean success, String errorMessage) {
        try {
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
            log.setRequestHash(DigestUtils.md5DigestAsHex((requestText == null ? "" : requestText).getBytes(StandardCharsets.UTF_8)));
            log.setCreatedAt(LocalDateTime.now());
            aiModelCallLogMapper.insert(log);
        } catch (RuntimeException ignored) {
            // AI 日志不能影响主业务流程。
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
