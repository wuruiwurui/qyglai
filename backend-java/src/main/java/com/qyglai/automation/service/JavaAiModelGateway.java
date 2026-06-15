package com.qyglai.automation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qyglai.automation.dto.AiRuntimeConfig;
import com.qyglai.automation.dto.AiRuntimeStatus;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Java 真实模型统一网关，支持按业务场景选择主模型并自动切换备用模型。
 */
@Service
public class JavaAiModelGateway {

    private final JavaAiModelConfigService configService;
    private final AiModelHealthService healthService;
    private final ObjectMapper objectMapper;
    private volatile String lastCallStatus = "not_called";
    private volatile String lastFallbackReason;
    private volatile String lastUsedModel;

    @Autowired
    public JavaAiModelGateway(JavaAiModelConfigService configService, AiModelHealthService healthService, ObjectMapper objectMapper) {
        this.configService = configService;
        this.healthService = healthService;
        this.objectMapper = objectMapper;
    }

    /**
     * 保留给独立单元测试和非Spring调用方的兼容构造函数。
     */
    public JavaAiModelGateway(JavaAiModelConfigService configService, ObjectMapper objectMapper) {
        this(configService, new AiModelHealthService(configService), objectMapper);
    }

    /** 使用默认路由调用模型并解析 JSON。 */
    public JsonNode generateJson(String systemPrompt, String userPrompt) {
        return generateJson("default", systemPrompt, userPrompt);
    }

    /**
     * 按场景路由调用模型并解析 JSON，主模型返回无效 JSON 时继续尝试备用模型。
     */
    public JsonNode generateJson(String scenario, String systemPrompt, String userPrompt) {
        // 路由服务会返回“主模型 + 备用模型”，健康模型优先，异常模型自动后移。
        List<AiRuntimeConfig> configs = healthService.orderByHealth(configService.resolveConfigs(scenario));
        String firstModel = firstModel(configs);
        String lastError = null;
        for (AiRuntimeConfig config : configs) {
            try {
                String content = callText(config, systemPrompt, userPrompt);
                // 模型有时会额外包裹 Markdown JSON 代码块，解析前统一剥离。
                String cleaned = content.trim().replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "");
                JsonNode node = objectMapper.readTree(cleaned);
                if (!node.isObject()) throw new IllegalStateException("模型返回内容不是JSON对象");
                markSuccess(firstModel, config.model());
                return node;
            } catch (Exception exception) {
                lastError = config.model() + ": " + exception.getMessage();
            }
        }
        markFailed(lastError == null ? "场景未配置可用真实模型" : "全部路由模型调用失败: " + lastError);
        return null;
    }

    /** 使用默认路由调用文本模型。 */
    public String generateText(String systemPrompt, String userPrompt, String fallback) {
        return generateText("default", systemPrompt, userPrompt, fallback);
    }

    /**
     * 按场景调用主模型，并在失败时依次切换备用模型。
     */
    public String generateText(String scenario, String systemPrompt, String userPrompt, String fallback) {
        List<AiRuntimeConfig> configs = healthService.orderByHealth(configService.resolveConfigs(scenario));
        String firstModel = firstModel(configs);
        String lastError = null;
        for (AiRuntimeConfig config : configs) {
            try {
                // 任一模型成功即结束路由；markSuccess 会记录是否发生了备用模型切换。
                String content = callText(config, systemPrompt, userPrompt);
                markSuccess(firstModel, config.model());
                return content;
            } catch (RuntimeException exception) {
                lastError = config.model() + ": " + exception.getMessage();
            }
        }
        lastCallStatus = "fallback";
        lastFallbackReason = lastError == null ? "场景未配置可用真实模型" : "全部路由模型调用失败: " + lastError;
        // 文本场景允许返回业务方传入的确定性兜底结果，避免外部模型故障阻断主流程。
        return fallback;
    }

    /**
     * 使用 OCR 场景路由调用多模态模型识别图片。
     */
    public String generateVisionText(String prompt, byte[] imageBytes, String mimeType) {
        List<AiRuntimeConfig> configs = healthService.orderByHealth(configService.resolveConfigs("document_ocr"));
        String firstModel = firstModel(configs);
        String lastError = null;
        for (AiRuntimeConfig config : configs) {
            try {
                String content = callVision(config, prompt, imageBytes, mimeType);
                markSuccess(firstModel, config.model());
                return content;
            } catch (RuntimeException exception) {
                lastError = config.model() + ": " + exception.getMessage();
            }
        }
        markFailed(lastError == null ? "OCR场景未配置可用多模态模型" : "全部OCR路由模型调用失败: " + lastError);
        throw new IllegalStateException(lastFallbackReason);
    }

    /**
     * 查询最近一次模型路由执行状态。
     */
    public AiRuntimeStatus status() {
        AiRuntimeConfig config = configService.getConfig();
        return new AiRuntimeStatus(config.provider(), lastUsedModel == null ? config.model() : lastUsedModel,
                config.enabled(), config.apiBase(), config.textGenerationEnabled(), config.fileExtractionMode(),
                config.contractRiskMode(), lastCallStatus, lastFallbackReason);
    }

    private String callText(AiRuntimeConfig config, String systemPrompt, String userPrompt) {
        validate(config, true);
        // 豆包兼容 OpenAI Chat Completions 协议，因此所有文本模型统一通过该结构调用。
        JsonNode response = client(config).post().uri("/chat/completions")
                .body(Map.of(
                        "model", config.model(),
                        "messages", List.of(
                                Map.of("role", "system", "content", systemPrompt),
                                Map.of("role", "user", "content", userPrompt)),
                        "temperature", 0.2))
                .retrieve().body(JsonNode.class);
        String content = response == null ? null : response.at("/choices/0/message/content").asText(null);
        if (content == null || content.isBlank()) throw new IllegalStateException("模型未返回有效文本");
        return content;
    }

    private String callVision(AiRuntimeConfig config, String prompt, byte[] imageBytes, String mimeType) {
        validate(config, false);
        // 图片转为 data URL 后与提示词一起提交，避免依赖外部可访问的临时图片地址。
        String dataUrl = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(imageBytes);
        JsonNode response = client(config).post().uri("/chat/completions")
                .body(Map.of(
                        "model", config.model(),
                        "messages", List.of(Map.of(
                                "role", "user",
                                "content", List.of(
                                        Map.of("type", "text", "text", prompt),
                                        Map.of("type", "image_url", "image_url", Map.of("url", dataUrl))))),
                        "temperature", 0))
                .retrieve().body(JsonNode.class);
        String content = response == null ? null : response.at("/choices/0/message/content").asText(null);
        if (content == null || content.isBlank()) throw new IllegalStateException("多模态模型未返回OCR文字");
        return content.strip();
    }

    private RestClient client(AiRuntimeConfig config) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(15));
        factory.setReadTimeout(Duration.ofSeconds(75));
        return RestClient.builder().baseUrl(config.apiBase().replaceAll("/+$", ""))
                .requestFactory(factory).defaultHeader("Authorization", "Bearer " + config.apiKey()).build();
    }

    /**
     * 在发起网络请求前检查模型开关、密钥、地址及文本生成能力。
     */
    private void validate(AiRuntimeConfig config, boolean requireTextGeneration) {
        if (!config.enabled() || config.apiKey() == null || config.apiBase() == null
                || "mock".equalsIgnoreCase(config.provider())
                || requireTextGeneration && !config.textGenerationEnabled()) {
            throw new IllegalStateException("真实模型未启用或缺少配置");
        }
    }

    private String firstModel(List<AiRuntimeConfig> configs) {
        return configs.isEmpty() ? null : configs.getFirst().model();
    }

    private void markSuccess(String firstModel, String usedModel) {
        // 保留实际使用的模型，供 AI 治理页面展示路由切换结果。
        lastUsedModel = usedModel;
        lastCallStatus = "success";
        lastFallbackReason = firstModel != null && !firstModel.equals(usedModel)
                ? "主模型 " + firstModel + " 调用失败，已自动切换备用模型 " + usedModel : null;
    }

    private void markFailed(String error) {
        lastCallStatus = "failed";
        lastFallbackReason = error == null ? "模型调用失败" : error;
    }
}
