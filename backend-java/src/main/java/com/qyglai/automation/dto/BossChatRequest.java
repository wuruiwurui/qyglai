package com.qyglai.automation.dto;

import jakarta.validation.constraints.NotBlank;

public record BossChatRequest(
        @NotBlank(message = "question is required")
        String question,
        String timeRange,
        String userId
) {
}

