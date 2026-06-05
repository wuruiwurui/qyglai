package com.qyglai.automation.dto;

public record TicketClassifyResult(
        String category,
        String priority,
        String sentiment,
        String suggestedOwner,
        double confidence
) {
}

