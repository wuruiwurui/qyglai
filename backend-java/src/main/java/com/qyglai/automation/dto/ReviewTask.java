package com.qyglai.automation.dto;

import java.time.Instant;

public record ReviewTask(
        String taskId,
        String scenario,
        String title,
        String riskLevel,
        String assignee,
        Instant createdAt
) {
}
