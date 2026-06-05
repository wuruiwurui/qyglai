package com.qyglai.automation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 文档解析结果保存请求。
 *
 * @param fileId 文件ID
 * @param rawText 原始解析文本
 */
public record DocParseSaveRequest(
        @NotNull(message = "fileId is required")
        Long fileId,
        @NotBlank(message = "rawText is required")
        String rawText
) {
}

