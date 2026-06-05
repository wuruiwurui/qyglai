package com.qyglai.automation.dto;

import java.util.List;

public record BossChatResponse(
        String intent,
        String answer,
        List<MetricCard> metrics,
        List<String> actions,
        List<String> sources
) {
}
