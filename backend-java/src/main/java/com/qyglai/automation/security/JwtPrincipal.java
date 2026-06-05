package com.qyglai.automation.security;

import java.util.List;

/**
 * JWT解析后的当前用户身份。
 *
 * @param userId 用户ID
 * @param username 用户名
 * @param roles 角色编码
 * @param permissions 权限编码
 */
public record JwtPrincipal(Long userId, String username, List<String> roles, List<String> permissions) {
}
