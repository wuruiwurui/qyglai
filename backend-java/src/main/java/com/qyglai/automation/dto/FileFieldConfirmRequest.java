package com.qyglai.automation.dto;

import java.util.Map;

import jakarta.validation.constraints.NotNull;

/**
 * 文件抽取字段人工确认请求。
 *
 * @param fields 人工确认后的抽取字段
 */
public record FileFieldConfirmRequest(
        @NotNull(message = "fields is required")
        Map<String, String> fields
) {
}
