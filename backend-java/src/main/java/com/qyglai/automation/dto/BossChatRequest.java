package com.qyglai.automation.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;

public record BossChatRequest(
        @NotBlank(message = "question is required")
        String question,
        String timeRange,
        String userId,
        Map<String, Object> context
) {
}
