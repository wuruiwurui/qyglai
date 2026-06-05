package com.qyglai.automation.dto;

import jakarta.validation.constraints.NotBlank;

public record KnowledgeQueryRequest(
        @NotBlank(message = "question is required")
        String question,
        String scope,
        String userId
) {
}

