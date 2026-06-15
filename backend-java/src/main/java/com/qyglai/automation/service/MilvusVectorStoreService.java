package com.qyglai.automation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.qyglai.automation.config.AutomationProperties;
import com.qyglai.automation.dto.KnowledgeVectorStatus;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Milvus向量存储服务，通过Milvus REST API维护知识切片向量。
 */
@Service
public class MilvusVectorStoreService {

    private final AutomationProperties.VectorStore properties;

    public MilvusVectorStoreService(AutomationProperties automationProperties) {
        this.properties = automationProperties.getVectorStore();
    }

    /** 确保知识库集合已经创建。 */
    public void ensureCollection() {
        JsonNode describe = post("/v2/vectordb/collections/describe", Map.of("collectionName", properties.getCollection()));
        if (describe.path("code").asInt(-1) == 0) return;
        JsonNode created = post("/v2/vectordb/collections/create", Map.of(
                "collectionName", properties.getCollection(),
                "dimension", properties.getDimension(),
                "metricType", "COSINE"));
        requireSuccess(created, "创建Milvus知识向量集合失败");
    }

    /** 写入或更新知识切片向量和检索所需动态字段。 */
    public void upsert(long chunkId, long documentId, long spaceId, List<Double> vector) {
        ensureCollection();
        JsonNode response = post("/v2/vectordb/entities/upsert", Map.of(
                "collectionName", properties.getCollection(),
                "data", List.of(Map.of(
                        "id", String.valueOf(chunkId),
                        "document_id", String.valueOf(documentId),
                        "space_id", String.valueOf(spaceId),
                        "vector", vector))));
        requireSuccess(response, "写入Milvus知识向量失败");
    }

    /** 删除并重新创建知识向量集合，用于完整重建和维度迁移。 */
    public void recreateCollection() {
        JsonNode dropped = post("/v2/vectordb/collections/drop", Map.of("collectionName", properties.getCollection()));
        if (dropped != null && dropped.path("code").asInt(-1) != 0
                && !dropped.path("message").asText("").toLowerCase().contains("not found")) {
            requireSuccess(dropped, "删除旧Milvus知识向量集合失败");
        }
        ensureCollection();
    }

    /** 执行COSINE Top-K向量检索，返回切片ID与相似度。 */
    public List<VectorHit> search(List<Double> vector, int limit) {
        ensureCollection();
        JsonNode response = post("/v2/vectordb/entities/search", Map.of(
                "collectionName", properties.getCollection(),
                "data", List.of(vector),
                "annsField", "vector",
                "limit", limit,
                "outputFields", List.of("id", "document_id", "space_id")));
        requireSuccess(response, "Milvus知识向量检索失败");
        List<VectorHit> hits = new ArrayList<>();
        response.path("data").forEach(item -> hits.add(new VectorHit(
                item.path("id").asLong(), item.path("document_id").asLong(), item.path("distance").asDouble())));
        return hits;
    }

    /** 返回当前真实向量链路状态。 */
    public KnowledgeVectorStatus status() {
        try {
            ensureCollection();
            return new KnowledgeVectorStatus(properties.isEnabled(), properties.getEmbeddingModel(),
                    properties.getDimension(), properties.getMilvusUrl(), properties.getCollection(),
                    true, "豆包Embedding与Milvus向量检索已就绪");
        } catch (Exception exception) {
            return new KnowledgeVectorStatus(properties.isEnabled(), properties.getEmbeddingModel(),
                    properties.getDimension(), properties.getMilvusUrl(), properties.getCollection(),
                    false, exception.getMessage());
        }
    }

    private JsonNode post(String path, Object body) {
        return client().post().uri(path).body(body).retrieve().body(JsonNode.class);
    }

    private RestClient client() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(20));
        return RestClient.builder().baseUrl(properties.getMilvusUrl().replaceAll("/+$", ""))
                .requestFactory(factory).build();
    }

    private void requireSuccess(JsonNode response, String message) {
        if (response == null || response.path("code").asInt(-1) != 0) {
            throw new IllegalStateException(message + ": " + (response == null ? "无响应" : response.path("message").asText()));
        }
    }

    /** Milvus向量检索命中。 */
    public record VectorHit(long chunkId, long documentId, double score) {
    }
}
