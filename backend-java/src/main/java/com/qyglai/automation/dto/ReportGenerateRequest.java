package com.qyglai.automation.dto;

import jakarta.validation.constraints.NotBlank;

public record ReportGenerateRequest(
        @NotBlank(message = "reportType is required")
        String reportType,
        String timeRange,
        String audience,
        boolean autoSend
) {
}

