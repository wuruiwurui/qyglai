package com.qyglai.automation.dto;

import java.time.Instant;

public record WorkflowInstanceSummary(
        String instanceId,
        String workflowCode,
        String currentNode,
        String status,
        Instant startedAt
) {
}

