package com.qyglai.automation.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 消息通知创建请求。
 *
 * @param channel 消息渠道
 * @param title 消息标题
 * @param content 消息内容
 */
public record NotificationCreateRequest(
        @NotBlank(message = "channel is required")
        String channel,
        @NotBlank(message = "title is required")
        String title,
        @NotBlank(message = "content is required")
        String content
) {
}

