package com.qyglai.automation.dto;

/**
 * 页面展示使用的Embedding配置，API Key仅返回脱敏内容。
 *
 * @param enabled 是否启用真实Embedding
 * @param provider 模型供应商
 * @param apiUrl Embedding完整调用地址
 * @param apiKeyMasked 脱敏API密钥
 * @param model Embedding模型或接入点ID
 * @param dimension 向量维度
 * @param milvusUrl Milvus REST地址
 * @param collection Milvus集合名称
 */
public record AiEmbeddingConfigView(
        boolean enabled,
        String provider,
        String apiUrl,
        String apiKeyMasked,
        String model,
        int dimension,
        String milvusUrl,
        String collection
) {
}
