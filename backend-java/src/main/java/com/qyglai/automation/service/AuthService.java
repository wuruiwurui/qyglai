package com.qyglai.automation.service;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qyglai.automation.dto.CurrentUser;
import com.qyglai.automation.dto.LoginRequest;
import com.qyglai.automation.dto.LoginResponse;
import com.qyglai.automation.entity.SysPermissionEntity;
import com.qyglai.automation.entity.SysOrgEntity;
import com.qyglai.automation.entity.SysRoleEntity;
import com.qyglai.automation.entity.SysRolePermissionEntity;
import com.qyglai.automation.entity.SysUserEntity;
import com.qyglai.automation.entity.SysUserRoleEntity;
import com.qyglai.automation.mapper.SysPermissionMapper;
import com.qyglai.automation.mapper.SysOrgMapper;
import com.qyglai.automation.mapper.SysRoleMapper;
import com.qyglai.automation.mapper.SysRolePermissionMapper;
import com.qyglai.automation.mapper.SysUserMapper;
import com.qyglai.automation.mapper.SysUserRoleMapper;
import com.qyglai.automation.security.JwtPrincipal;
import com.qyglai.automation.security.JwtTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证与当前用户服务。
 */
@Service
public class AuthService {

    private final SysUserMapper userMapper;
    private final SysOrgMapper orgMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokenService;

    public AuthService(SysUserMapper userMapper, SysOrgMapper orgMapper, SysRoleMapper roleMapper, SysPermissionMapper permissionMapper,
                       SysUserRoleMapper userRoleMapper, SysRolePermissionMapper rolePermissionMapper,
                       PasswordEncoder passwordEncoder, JwtTokenService tokenService) {
        this.userMapper = userMapper;
        this.orgMapper = orgMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.userRoleMapper = userRoleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    /**
     * 用户登录。
     *
     * @param request 登录请求
     * @return 登录令牌和用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request) {
        ensureBootstrapAdmin();
        // 仅允许状态正常的用户登录，并使用 BCrypt 校验密码散列。
        SysUserEntity user = userMapper.selectOne(new LambdaQueryWrapper<SysUserEntity>()
                .eq(SysUserEntity::getUsername, request.username())
                .eq(SysUserEntity::getStatus, "enabled")
                .last("limit 1"));
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        List<String> roles = listRoleCodes(user.getId());
        List<String> permissions = listPermissionCodes(user.getId());
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);
        // 角色和权限编码写入 JWT，后续请求无需每次重新查询权限关系表。
        String token = tokenService.createToken(user.getId(), user.getUsername(), roles, permissions);
        return new LoginResponse(token, "Bearer", tokenService.ttlSeconds(), toCurrentUser(user), roles, permissions);
    }

    /**
     * 根据JWT身份查询当前用户。
     *
     * @param principal JWT身份
     * @return 登录响应风格的当前用户信息
     */
    public LoginResponse me(JwtPrincipal principal) {
        SysUserEntity user = userMapper.selectById(principal.userId());
        return new LoginResponse("", "Bearer", tokenService.ttlSeconds(), toCurrentUser(user), principal.roles(), principal.permissions());
    }

    private CurrentUser toCurrentUser(SysUserEntity user) {
        return new CurrentUser(user.getId(), user.getUsername(), user.getRealName(), user.getOrgId(), user.getAvatarUrl());
    }

    private List<String> listRoleCodes(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRoleEntity>().eq(SysUserRoleEntity::getUserId, userId))
                .stream().map(SysUserRoleEntity::getRoleId).toList();
        if (roleIds.isEmpty()) {
            // 兼容初始演示环境：尚未配置角色关系的首个管理员拥有管理权限。
            return List.of("ADMIN");
        }
        return roleMapper.selectBatchIds(roleIds).stream().map(SysRoleEntity::getRoleCode).toList();
    }

    private List<String> listPermissionCodes(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRoleEntity>().eq(SysUserRoleEntity::getUserId, userId))
                .stream().map(SysUserRoleEntity::getRoleId).toList();
        if (roleIds.isEmpty()) {
            return List.of("*");
        }
        List<Long> permissionIds = rolePermissionMapper.selectList(new LambdaQueryWrapper<SysRolePermissionEntity>().in(SysRolePermissionEntity::getRoleId, roleIds))
                .stream().map(SysRolePermissionEntity::getPermissionId).toList();
        if (permissionIds.isEmpty()) {
            return List.of("*");
        }
        return permissionMapper.selectBatchIds(permissionIds).stream().map(SysPermissionEntity::getPermissionCode).toList();
    }

    /**
     * 初始化默认管理员账号，确保本地和演示环境可以直接登录。
     */
    private void ensureBootstrapAdmin() {
        SysUserEntity existing = userMapper.selectOne(new LambdaQueryWrapper<SysUserEntity>().eq(SysUserEntity::getUsername, "admin").last("limit 1"));
        if (existing != null) {
            return;
        }
        // 仅在数据库没有 admin 时初始化，避免覆盖用户后来修改的管理员信息和密码。
        SysUserEntity admin = new SysUserEntity();
        admin.setOrgId(ensureRootOrg());
        admin.setUsername("admin");
        admin.setRealName("系统管理员");
        admin.setPasswordHash(passwordEncoder.encode("123456"));
        admin.setMobile("13800000000");
        admin.setEmail("admin@qyglai.local");
        admin.setStatus("enabled");
        admin.setDeleted(0);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(admin);
    }

    private Long ensureRootOrg() {
        SysOrgEntity existing = orgMapper.selectOne(new LambdaQueryWrapper<SysOrgEntity>().eq(SysOrgEntity::getOrgCode, "ROOT").last("limit 1"));
        if (existing != null) {
            return existing.getId();
        }
        SysOrgEntity org = new SysOrgEntity();
        org.setParentId(0L);
        org.setOrgCode("ROOT");
        org.setOrgName("默认企业");
        org.setOrgType("company");
        org.setSortOrder(1);
        org.setStatus("enabled");
        org.setDeleted(0);
        org.setCreatedAt(LocalDateTime.now());
        org.setUpdatedAt(LocalDateTime.now());
        orgMapper.insert(org);
        return org.getId();
    }
}
