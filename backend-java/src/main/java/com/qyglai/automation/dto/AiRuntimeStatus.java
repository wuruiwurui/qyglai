package com.qyglai.automation.dto;

/**
 * Java AI 模型最近运行状态。
 */
public record AiRuntimeStatus(
        String provider,
        String model,
        boolean enabled,
        String apiBase,
        boolean textGenerationEnabled,
        String fileExtractionMode,
        String contractRiskMode,
        String lastCallStatus,
        String lastFallbackReason
) {
}
