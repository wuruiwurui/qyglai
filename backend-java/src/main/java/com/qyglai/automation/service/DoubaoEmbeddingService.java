package com.qyglai.automation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.qyglai.automation.config.AutomationProperties;
import com.qyglai.automation.dto.AiRuntimeConfig;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * 豆包真实Embedding服务，复用当前真实模型配置中的API Key。
 */
@Service
public class DoubaoEmbeddingService {

    private final AutomationProperties.VectorStore properties;
    private final JavaAiModelConfigService modelConfigService;
    private final AiCallLogService aiCallLogService;

    public DoubaoEmbeddingService(AutomationProperties automationProperties,
                                  JavaAiModelConfigService modelConfigService,
                                  AiCallLogService aiCallLogService) {
        this.properties = automationProperties.getVectorStore();
        this.modelConfigService = modelConfigService;
        this.aiCallLogService = aiCallLogService;
    }

    /** 调用豆包多模态Embedding接口生成2048维真实语义向量。 */
    public List<Double> embed(String text) {
        if (!properties.isEnabled()) throw new IllegalStateException("真实向量服务未启用");
        AiRuntimeConfig modelConfig = modelConfigService.getConfig();
        if (modelConfig.apiKey() == null || modelConfig.apiKey().isBlank()) {
            throw new IllegalStateException("当前模型配置缺少豆包API Key");
        }
        long start = System.currentTimeMillis();
        try {
            JsonNode response = client(modelConfig.apiKey()).post()
                    .body(Map.of(
                            "model", properties.getEmbeddingModel(),
                            "input", List.of(Map.of("type", "text", "text", text))))
                    .retrieve().body(JsonNode.class);
            JsonNode embedding = response == null ? null : response.path("data").path("embedding");
            if (embedding == null || !embedding.isArray() || embedding.size() != properties.getDimension()) {
                throw new IllegalStateException("Embedding返回维度异常，期望 " + properties.getDimension());
            }
            List<Double> vector = new ArrayList<>(embedding.size());
            embedding.forEach(value -> vector.add(value.asDouble()));
            aiCallLogService.record("knowledge_embedding", "knowledge", null,
                    properties.getEmbeddingModel(), "doubao/embeddings/multimodal",
                    text, "dimension=" + vector.size(), start, true, null);
            return vector;
        } catch (RuntimeException exception) {
            aiCallLogService.record("knowledge_embedding", "knowledge", null,
                    properties.getEmbeddingModel(), "doubao/embeddings/multimodal",
                    text, null, start, false, exception.getMessage());
            throw exception;
        }
    }

    public String model() {
        return properties.getEmbeddingModel();
    }

    private RestClient client(String apiKey) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(12));
        factory.setReadTimeout(Duration.ofSeconds(45));
        return RestClient.builder().baseUrl(properties.getEmbeddingUrl())
                .requestFactory(factory).defaultHeader("Authorization", "Bearer " + apiKey).build();
    }
}
