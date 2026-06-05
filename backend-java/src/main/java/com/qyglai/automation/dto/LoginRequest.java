package com.qyglai.automation.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求。
 *
 * @param username 登录用户名
 * @param password 登录密码
 */
public record LoginRequest(
        @NotBlank(message = "username is required")
        String username,
        @NotBlank(message = "password is required")
        String password
) {
}
