package com.qyglai.automation.dto;

import java.util.List;

/**
 * 登录响应。
 *
 * @param token JWT访问令牌
 * @param tokenType 令牌类型
 * @param expiresIn 过期秒数
 * @param user 当前用户
 * @param roles 角色编码列表
 * @param permissions 权限编码列表
 */
public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn,
        CurrentUser user,
        List<String> roles,
        List<String> permissions
) {
}
