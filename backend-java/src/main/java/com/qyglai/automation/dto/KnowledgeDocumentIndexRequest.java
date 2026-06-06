package com.qyglai.automation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 知识库纯文本入库请求。
 *
 * @param spaceId 知识库空间ID
 * @param title 文档标题
 * @param content 文档正文
 * @param sourceUrl 来源地址
 */
public record KnowledgeDocumentIndexRequest(
        @NotNull(message = "spaceId is required") Long spaceId,
        @NotBlank(message = "title is required") String title,
        @NotBlank(message = "content is required") String content,
        String sourceUrl
) {
}
