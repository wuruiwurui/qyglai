package com.qyglai.automation.service;

import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qyglai.automation.dto.AiRuntimeConfig;
import com.qyglai.automation.dto.AiRuntimeStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Java 直接访问豆包或其他 OpenAI 兼容模型的统一网关。
 */
@Service
public class JavaAiModelGateway {

    private final JavaAiModelConfigService configService;
    private final ObjectMapper objectMapper;
    private volatile String lastCallStatus = "not_called";
    private volatile String lastFallbackReason;

    public JavaAiModelGateway(JavaAiModelConfigService configService, ObjectMapper objectMapper) {
        this.configService = configService;
        this.objectMapper = objectMapper;
    }

    /**
     * 调用真实模型并解析 JSON 对象。
     */
    public JsonNode generateJson(String systemPrompt, String userPrompt) {
        String content = generateText(systemPrompt, userPrompt, "");
        if (content == null || content.isBlank() || !"success".equals(lastCallStatus)) return null;
        try {
            String cleaned = content.trim()
                    .replaceFirst("^```(?:json)?\\s*", "")
                    .replaceFirst("\\s*```$", "");
            JsonNode node = objectMapper.readTree(cleaned);
            if (!node.isObject()) throw new IllegalStateException("模型返回内容不是JSON对象");
            return node;
        } catch (Exception ex) {
            lastCallStatus = "fallback";
            lastFallbackReason = "模型JSON解析失败: " + ex.getMessage();
            return null;
        }
    }

    /**
     * 调用真实模型生成文本，失败时返回业务层提供的数据库摘要。
     */
    public String generateText(String systemPrompt, String userPrompt, String fallback) {
        AiRuntimeConfig config = configService.getConfig();
        if (!config.enabled() || !config.textGenerationEnabled() || config.apiKey() == null
                || config.apiBase() == null || "mock".equalsIgnoreCase(config.provider())) {
            lastCallStatus = "fallback";
            lastFallbackReason = "Java真实模型未启用或缺少配置";
            return fallback;
        }
        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(Duration.ofSeconds(15));
            factory.setReadTimeout(Duration.ofSeconds(75));
            RestClient client = RestClient.builder()
                    .baseUrl(config.apiBase().replaceAll("/+$", ""))
                    .requestFactory(factory)
                    .defaultHeader("Authorization", "Bearer " + config.apiKey())
                    .build();
            JsonNode response = client.post()
                    .uri("/chat/completions")
                    .body(Map.of(
                            "model", config.model(),
                            "messages", List.of(
                                    Map.of("role", "system", "content", systemPrompt),
                                    Map.of("role", "user", "content", userPrompt)
                            ),
                            "temperature", 0.2
                    ))
                    .retrieve()
                    .body(JsonNode.class);
            String content = response == null ? null : response.at("/choices/0/message/content").asText(null);
            if (content == null || content.isBlank()) throw new IllegalStateException("模型未返回有效文本");
            lastCallStatus = "success";
            lastFallbackReason = null;
            return content;
        } catch (RuntimeException ex) {
            lastCallStatus = "fallback";
            lastFallbackReason = ex.getMessage();
            return fallback;
        }
    }

    /**
     * 调用当前多模态模型识别图片文字。
     *
     * @param prompt OCR 提示词
     * @param imageBytes 图片字节
     * @param mimeType 图片 MIME 类型
     * @return 模型识别文字
     */
    public String generateVisionText(String prompt, byte[] imageBytes, String mimeType) {
        AiRuntimeConfig config = configService.getConfig();
        if (!config.enabled() || config.apiKey() == null || config.apiBase() == null
                || "mock".equalsIgnoreCase(config.provider())) {
            throw new IllegalStateException("OCR需要启用支持图片理解的真实模型");
        }
        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(Duration.ofSeconds(15));
            factory.setReadTimeout(Duration.ofSeconds(120));
            RestClient client = RestClient.builder()
                    .baseUrl(config.apiBase().replaceAll("/+$", ""))
                    .requestFactory(factory)
                    .defaultHeader("Authorization", "Bearer " + config.apiKey())
                    .build();
            String dataUrl = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(imageBytes);
            JsonNode response = client.post()
                    .uri("/chat/completions")
                    .body(Map.of(
                            "model", config.model(),
                            "messages", List.of(Map.of(
                                    "role", "user",
                                    "content", List.of(
                                            Map.of("type", "text", "text", prompt),
                                            Map.of("type", "image_url", "image_url", Map.of("url", dataUrl))
                                    )
                            )),
                            "temperature", 0
                    ))
                    .retrieve()
                    .body(JsonNode.class);
            String content = response == null ? null : response.at("/choices/0/message/content").asText(null);
            if (content == null || content.isBlank()) throw new IllegalStateException("多模态模型未返回OCR文字");
            lastCallStatus = "success";
            lastFallbackReason = null;
            return content.strip();
        } catch (RuntimeException ex) {
            lastCallStatus = "failed";
            lastFallbackReason = "OCR模型调用失败: " + ex.getMessage();
            throw new IllegalStateException(lastFallbackReason, ex);
        }
    }

    public AiRuntimeStatus status() {
        AiRuntimeConfig config = configService.getConfig();
        return new AiRuntimeStatus(config.provider(), config.model(), config.enabled(), config.apiBase(),
                config.textGenerationEnabled(), config.fileExtractionMode(), config.contractRiskMode(),
                lastCallStatus, lastFallbackReason);
    }
}
