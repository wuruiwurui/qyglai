package com.qyglai.automation.dto;

import java.time.LocalDateTime;

public record SalesFollowupTask(
        String taskId,
        String customerName,
        String opportunityStage,
        String nextAction,
        LocalDateTime dueTime,
        String owner,
        String status
) {
}

