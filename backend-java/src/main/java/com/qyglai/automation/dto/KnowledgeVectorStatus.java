package com.qyglai.automation.dto;

/**
 * 知识库真实向量链路运行状态。
 *
 * @param enabled 是否启用真实向量检索
 * @param embeddingModel 豆包Embedding接入点ID
 * @param dimension 向量维度
 * @param milvusUrl Milvus连接地址
 * @param collection Milvus集合名称
 * @param milvusHealthy Milvus是否连接正常
 * @param message 状态说明
 */
public record KnowledgeVectorStatus(
        boolean enabled,
        String embeddingModel,
        int dimension,
        String milvusUrl,
        String collection,
        boolean milvusHealthy,
        String message
) {
}
