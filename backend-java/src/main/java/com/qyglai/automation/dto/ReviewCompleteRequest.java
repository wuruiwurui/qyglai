package com.qyglai.automation.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 人工复核完成请求。
 *
 * @param result 复核结果
 */
public record ReviewCompleteRequest(
        @NotBlank(message = "result is required")
        String result
) {
}

