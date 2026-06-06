package com.qyglai.automation.dto;

/**
 * 知识库向量检索命中结果。
 *
 * @param chunkId 切片ID
 * @param documentId 文档ID
 * @param title 文档标题
 * @param content 命中内容
 * @param score 相似度
 * @param sourceUrl 来源地址
 */
public record KnowledgeSearchHit(
        Long chunkId,
        Long documentId,
        String title,
        String content,
        double score,
        String sourceUrl
) {
}
