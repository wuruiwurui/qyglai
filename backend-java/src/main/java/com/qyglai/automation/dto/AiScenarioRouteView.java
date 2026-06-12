package com.qyglai.automation.dto;

import java.util.List;

/**
 * AI 场景模型路由页面视图。
 *
 * @param scenario 场景编码
 * @param primaryProfileId 主模型配置 ID
 * @param primaryModel 主模型名称
 * @param fallbackProfileIds 备用模型配置 ID
 * @param fallbackModels 备用模型名称
 */
public record AiScenarioRouteView(
        String scenario,
        String primaryProfileId,
        String primaryModel,
        List<String> fallbackProfileIds,
        List<String> fallbackModels
) {
}
