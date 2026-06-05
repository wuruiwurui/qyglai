package com.qyglai.automation.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;

public record WorkflowStartRequest(
        @NotBlank(message = "workflowCode is required")
        String workflowCode,
        String initiator,
        Map<String, Object> variables
) {
}

