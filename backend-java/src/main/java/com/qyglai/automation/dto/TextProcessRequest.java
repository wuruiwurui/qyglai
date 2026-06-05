package com.qyglai.automation.dto;

import jakarta.validation.constraints.NotBlank;

public record TextProcessRequest(
        @NotBlank(message = "content is required")
        String content,
        String scenario,
        String operatorId
) {
}

