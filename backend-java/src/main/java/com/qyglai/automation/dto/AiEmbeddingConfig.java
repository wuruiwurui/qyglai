package com.qyglai.automation.dto;

/**
 * Embedding与向量数据库运行配置。
 *
 * @param enabled 是否启用真实Embedding
 * @param provider 模型供应商
 * @param apiUrl Embedding完整调用地址
 * @param apiKey API密钥
 * @param model Embedding模型或接入点ID
 * @param dimension 向量维度
 * @param milvusUrl Milvus REST地址
 * @param collection Milvus集合名称
 */
public record AiEmbeddingConfig(
        boolean enabled,
        String provider,
        String apiUrl,
        String apiKey,
        String model,
        int dimension,
        String milvusUrl,
        String collection
) {
}
