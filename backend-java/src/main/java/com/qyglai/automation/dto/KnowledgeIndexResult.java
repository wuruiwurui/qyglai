package com.qyglai.automation.dto;

/**
 * 知识库文档索引结果。
 *
 * @param documentId 文档ID
 * @param title 文档标题
 * @param chunkCount 切片数量
 * @param indexingStatus 索引状态
 */
public record KnowledgeIndexResult(
        Long documentId,
        String title,
        int chunkCount,
        String indexingStatus
) {
}
