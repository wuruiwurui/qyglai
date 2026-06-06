package com.qyglai.automation.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 审批动作请求。
 *
 * @param action 动作类型：approve、reject、transfer
 * @param comment 审批意见
 * @param targetUserId 转交目标用户ID
 */
public record WorkflowActionRequest(
        @NotBlank(message = "action is required")
        String action,
        String comment,
        Long targetUserId
) {
}
