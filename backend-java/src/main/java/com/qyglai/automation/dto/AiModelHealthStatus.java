package com.qyglai.automation.dto;

import java.time.LocalDateTime;

/**
 * 模型连接健康状态。
 *
 * @param profileId 模型配置ID
 * @param profileName 模型配置名称
 * @param model 模型或Endpoint ID
 * @param status 健康状态：healthy、unhealthy、unknown
 * @param latencyMs 健康探测耗时
 * @param message 探测结果说明
 * @param checkedAt 最近探测时间
 */
public record AiModelHealthStatus(
        String profileId,
        String profileName,
        String model,
        String status,
        long latencyMs,
        String message,
        LocalDateTime checkedAt
) {
}
