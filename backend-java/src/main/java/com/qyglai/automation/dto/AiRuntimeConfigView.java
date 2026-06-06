package com.qyglai.automation.dto;

/**
 * 脱敏后的 Java AI 模型配置。
 */
public record AiRuntimeConfigView(
        String provider,
        String apiBase,
        String apiKeyMasked,
        String model,
        boolean enabled,
        boolean textGenerationEnabled,
        String fileExtractionMode,
        String contractRiskMode,
        String remark
) {
}
