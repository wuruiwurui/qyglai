package com.qyglai.automation.dto;

import java.util.List;

/**
 * AI 业务场景模型路由配置。
 *
 * @param scenario 场景编码
 * @param primaryProfileId 主模型配置 ID
 * @param fallbackProfileIds 按顺序执行的备用模型配置 ID
 */
public record AiScenarioRoute(
        String scenario,
        String primaryProfileId,
        List<String> fallbackProfileIds
) {
}
