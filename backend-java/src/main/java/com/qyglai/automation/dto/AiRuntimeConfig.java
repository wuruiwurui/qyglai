package com.qyglai.automation.dto;

/**
 * Java AI 模型运行配置。
 */
public record AiRuntimeConfig(
        String provider,
        String apiBase,
        String apiKey,
        String model,
        boolean enabled,
        boolean textGenerationEnabled,
        String fileExtractionMode,
        String contractRiskMode,
        String remark
) {
}
