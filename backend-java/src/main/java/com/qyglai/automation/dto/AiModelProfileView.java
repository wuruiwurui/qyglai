package com.qyglai.automation.dto;

/**
 * 页面展示的模型配置，API密钥仅返回脱敏值。
 *
 * @param id 配置唯一标识
 * @param name 页面展示名称
 * @param provider 模型供应商
 * @param apiBase API基础地址
 * @param apiKeyMasked 脱敏API密钥
 * @param model 模型名称或Endpoint ID
 * @param enabled 是否允许调用
 * @param current 是否为当前使用模型
 * @param textGenerationEnabled 文本生成是否调用真实模型
 * @param fileExtractionMode 文件抽取策略
 * @param contractRiskMode 合同风险识别策略
 * @param remark 配置备注
 */
public record AiModelProfileView(
        String id,
        String name,
        String provider,
        String apiBase,
        String apiKeyMasked,
        String model,
        boolean enabled,
        boolean current,
        boolean textGenerationEnabled,
        String fileExtractionMode,
        String contractRiskMode,
        String remark
) {
}
