package com.qyglai.automation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.qyglai.automation.dto.AiEmbeddingConfig;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * 豆包真实Embedding服务，运行配置来自数据库中的AI治理配置。
 */
@Service
public class DoubaoEmbeddingService {

    private final AiEmbeddingConfigService configService;
    private final AiCallLogService aiCallLogService;

    public DoubaoEmbeddingService(AiEmbeddingConfigService configService, AiCallLogService aiCallLogService) {
        this.configService = configService;
        this.aiCallLogService = aiCallLogService;
    }

    /** 调用豆包多模态Embedding接口生成2048维真实语义向量。 */
    public List<Double> embed(String text) {
        AiEmbeddingConfig config = configService.getConfig();
        if (!config.enabled()) throw new IllegalStateException("真实向量服务未启用");
        if (config.apiKey() == null || config.apiKey().isBlank()) {
            throw new IllegalStateException("Embedding配置缺少API Key");
        }
        long start = System.currentTimeMillis();
        try {
            // 使用 AI 治理页面保存的接入点 ID 和密钥调用豆包多模态 Embedding 接口。
            JsonNode response = client(config).post()
                    .body(Map.of(
                            "model", config.model(),
                            "input", List.of(Map.of("type", "text", "text", text))))
                    .retrieve().body(JsonNode.class);
            JsonNode embedding = response == null ? null : response.path("data").path("embedding");
            // 写入 Milvus 前必须校验维度，防止错误向量破坏集合的一致性。
            if (embedding == null || !embedding.isArray() || embedding.size() != config.dimension()) {
                throw new IllegalStateException("Embedding返回维度异常，期望 " + config.dimension());
            }
            List<Double> vector = new ArrayList<>(embedding.size());
            embedding.forEach(value -> vector.add(value.asDouble()));
            aiCallLogService.record("knowledge_embedding", "knowledge", null,
                    config.model(), "doubao/embeddings/multimodal",
                    text, "dimension=" + vector.size(), start, true, null);
            return vector;
        } catch (RuntimeException exception) {
            // 失败日志同样写入模型调用表，便于在 AI 治理页面定位接入点或网络问题。
            aiCallLogService.record("knowledge_embedding", "knowledge", null,
                    config.model(), "doubao/embeddings/multimodal",
                    text, null, start, false, exception.getMessage());
            throw exception;
        }
    }

    public String model() {
        return configService.getConfig().model();
    }

    /** 按当前数据库配置创建带超时和 Bearer Token 的 Embedding HTTP 客户端。 */
    private RestClient client(AiEmbeddingConfig config) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(12));
        factory.setReadTimeout(Duration.ofSeconds(45));
        return RestClient.builder().baseUrl(config.apiUrl())
                .requestFactory(factory).defaultHeader("Authorization", "Bearer " + config.apiKey()).build();
    }
}
