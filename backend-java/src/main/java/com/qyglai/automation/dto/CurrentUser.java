package com.qyglai.automation.dto;

/**
 * 当前登录用户信息。
 *
 * @param id 用户ID
 * @param username 用户名
 * @param realName 真实姓名
 * @param orgId 组织ID
 * @param avatarUrl 头像地址
 */
public record CurrentUser(
        Long id,
        String username,
        String realName,
        Long orgId,
        String avatarUrl
) {
}
