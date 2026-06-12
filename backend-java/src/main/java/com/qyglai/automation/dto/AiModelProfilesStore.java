package com.qyglai.automation.dto;

import java.util.List;

/**
 * 多模型运行配置持久化结构。
 *
 * @param currentProfileId 当前使用的模型配置ID
 * @param profiles 全部模型配置
 * @param scenarioRoutes 业务场景模型路由
 */
public record AiModelProfilesStore(
        String currentProfileId,
        List<AiModelProfile> profiles,
        List<AiScenarioRoute> scenarioRoutes
) {
}
