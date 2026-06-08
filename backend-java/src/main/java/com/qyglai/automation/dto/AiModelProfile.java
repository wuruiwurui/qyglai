package com.qyglai.automation.dto;

/**
 * 可切换的真实模型配置。
 *
 * @param id 配置唯一标识
 * @param name 页面展示名称
 * @param provider 模型供应商
 * @param apiBase API基础地址
 * @param apiKey API密钥
 * @param model 模型名称或Endpoint ID
 * @param enabled 是否允许调用
 * @param textGenerationEnabled 文本生成是否调用真实模型
 * @param fileExtractionMode 文件抽取策略
 * @param contractRiskMode 合同风险识别策略
 * @param remark 配置备注
 */
public record AiModelProfile(
        String id,
        String name,
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
