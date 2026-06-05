package com.qyglai.automation.service;

import java.util.List;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.qyglai.automation.dto.SimpleCreateRequest;
import com.qyglai.automation.dto.SystemOrgSaveRequest;
import com.qyglai.automation.dto.SystemPermissionSaveRequest;
import com.qyglai.automation.dto.SystemRoleSaveRequest;
import com.qyglai.automation.dto.SystemUserSaveRequest;
import com.qyglai.automation.entity.SysOrgEntity;
import com.qyglai.automation.entity.SysPermissionEntity;
import com.qyglai.automation.entity.SysRoleEntity;
import com.qyglai.automation.entity.SysRolePermissionEntity;
import com.qyglai.automation.entity.SysUserEntity;
import com.qyglai.automation.entity.SysUserRoleEntity;
import com.qyglai.automation.mapper.SysOrgMapper;
import com.qyglai.automation.mapper.SysPermissionMapper;
import com.qyglai.automation.mapper.SysRoleMapper;
import com.qyglai.automation.mapper.SysRolePermissionMapper;
import com.qyglai.automation.mapper.SysUserMapper;
import com.qyglai.automation.mapper.SysUserRoleMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统管理服务，负责组织、用户、角色和权限的基础维护。
 */
@Service
public class SystemManagementService {

    private final SysOrgMapper orgMapper;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public SystemManagementService(SysOrgMapper orgMapper, SysUserMapper userMapper, SysRoleMapper roleMapper,
                                   SysPermissionMapper permissionMapper, SysUserRoleMapper userRoleMapper,
                                   SysRolePermissionMapper rolePermissionMapper, PasswordEncoder passwordEncoder,
                                   AuditService auditService) {
        this.orgMapper = orgMapper;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.userRoleMapper = userRoleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    /**
     * 创建组织。
     *
     * @param request 创建请求
     * @return 组织实体
     */
    public SysOrgEntity createOrg(SimpleCreateRequest request) {
        SysOrgEntity org = new SysOrgEntity();
        org.setOrgCode(request.code() == null ? "ORG-" + IdWorker.getId() : request.code());
        org.setOrgName(request.name() == null ? "未命名组织" : request.name());
        org.setOrgType(request.type() == null ? "department" : request.type());
        org.setParentId(0L);
        org.setStatus("enabled");
        orgMapper.insert(org);
        auditService.record("SYS_ORG_CREATE", "创建组织", "sys_org", org.getId());
        return org;
    }

    /**
     * 查询组织列表。
     *
     * @return 组织列表
     */
    public List<SysOrgEntity> listOrgs() {
        return orgMapper.selectList(new LambdaQueryWrapper<SysOrgEntity>().orderByAsc(SysOrgEntity::getSortOrder));
    }

    /**
     * 查询用户列表。
     *
     * @return 用户列表
     */
    public List<SysUserEntity> listUsers() {
        return userMapper.selectList(new LambdaQueryWrapper<SysUserEntity>().orderByDesc(SysUserEntity::getCreatedAt));
    }

    /**
     * 查询角色列表。
     *
     * @return 角色列表
     */
    public List<SysRoleEntity> listRoles() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRoleEntity>().orderByDesc(SysRoleEntity::getCreatedAt));
    }

    /**
     * 查询权限列表。
     *
     * @return 权限列表
     */
    public List<SysPermissionEntity> listPermissions() {
        return permissionMapper.selectList(new LambdaQueryWrapper<SysPermissionEntity>().orderByAsc(SysPermissionEntity::getSortOrder));
    }

    public SysOrgEntity saveOrg(Long id, SystemOrgSaveRequest request) {
        SysOrgEntity org = id == null ? new SysOrgEntity() : requireOrg(id);
        org.setParentId(request.parentId() == null ? 0L : request.parentId());
        org.setOrgCode(request.orgCode());
        org.setOrgName(request.orgName());
        org.setOrgType(defaultValue(request.orgType(), "department"));
        org.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        org.setStatus(defaultValue(request.status(), "enabled"));
        if (id == null) {
            org.setId(IdWorker.getId());
            org.setDeleted(0);
            org.setCreatedAt(LocalDateTime.now());
            orgMapper.insert(org);
        } else {
            org.setUpdatedAt(LocalDateTime.now());
            orgMapper.updateById(org);
        }
        auditService.record("SYS_ORG_SAVE", "保存组织", "sys_org", org.getId());
        return org;
    }

    public SysUserEntity saveUser(Long id, SystemUserSaveRequest request) {
        SysUserEntity user = id == null ? new SysUserEntity() : requireUser(id);
        user.setOrgId(request.orgId());
        user.setUsername(request.username());
        user.setRealName(request.realName());
        user.setMobile(request.mobile());
        user.setEmail(request.email());
        user.setStatus(defaultValue(request.status(), "enabled"));
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        if (id == null) {
            user.setId(IdWorker.getId());
            if (user.getPasswordHash() == null) user.setPasswordHash(passwordEncoder.encode("123456"));
            user.setDeleted(0);
            user.setCreatedAt(LocalDateTime.now());
            userMapper.insert(user);
        } else {
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
        }
        auditService.record("SYS_USER_SAVE", "保存用户", "sys_user", user.getId());
        return user;
    }

    public SysRoleEntity saveRole(Long id, SystemRoleSaveRequest request) {
        SysRoleEntity role = id == null ? new SysRoleEntity() : requireRole(id);
        role.setRoleCode(request.roleCode());
        role.setRoleName(request.roleName());
        role.setDataScope(defaultValue(request.dataScope(), "self"));
        role.setStatus(defaultValue(request.status(), "enabled"));
        if (id == null) {
            role.setId(IdWorker.getId());
            role.setDeleted(0);
            role.setCreatedAt(LocalDateTime.now());
            roleMapper.insert(role);
        } else {
            role.setUpdatedAt(LocalDateTime.now());
            roleMapper.updateById(role);
        }
        auditService.record("SYS_ROLE_SAVE", "保存角色", "sys_role", role.getId());
        return role;
    }

    public SysPermissionEntity savePermission(Long id, SystemPermissionSaveRequest request) {
        SysPermissionEntity permission = id == null ? new SysPermissionEntity() : requirePermission(id);
        permission.setPermissionCode(request.permissionCode());
        permission.setPermissionName(request.permissionName());
        permission.setPermissionType(defaultValue(request.permissionType(), "api"));
        permission.setResourcePath(request.resourcePath());
        permission.setParentId(request.parentId() == null ? 0L : request.parentId());
        permission.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        permission.setStatus(defaultValue(request.status(), "enabled"));
        if (id == null) {
            permission.setId(IdWorker.getId());
            permission.setDeleted(0);
            permission.setCreatedAt(LocalDateTime.now());
            permissionMapper.insert(permission);
        } else {
            permission.setUpdatedAt(LocalDateTime.now());
            permissionMapper.updateById(permission);
        }
        auditService.record("SYS_PERMISSION_SAVE", "保存权限", "sys_permission", permission.getId());
        return permission;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Long> assignUserRoles(Long userId, List<Long> roleIds) {
        requireUser(userId);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRoleEntity>().eq(SysUserRoleEntity::getUserId, userId));
        for (Long roleId : roleIds) {
            requireRole(roleId);
            SysUserRoleEntity relation = new SysUserRoleEntity();
            relation.setId(IdWorker.getId());
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            relation.setCreatedAt(LocalDateTime.now());
            userRoleMapper.insert(relation);
        }
        auditService.record("SYS_USER_ROLE_ASSIGN", "分配用户角色", "sys_user", userId);
        return roleIds;
    }

    public List<Long> listUserRoleIds(Long userId) {
        return userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRoleEntity>().eq(SysUserRoleEntity::getUserId, userId))
                .stream().map(SysUserRoleEntity::getRoleId).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Long> assignRolePermissions(Long roleId, List<Long> permissionIds) {
        requireRole(roleId);
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermissionEntity>().eq(SysRolePermissionEntity::getRoleId, roleId));
        for (Long permissionId : permissionIds) {
            requirePermission(permissionId);
            SysRolePermissionEntity relation = new SysRolePermissionEntity();
            relation.setId(IdWorker.getId());
            relation.setRoleId(roleId);
            relation.setPermissionId(permissionId);
            relation.setCreatedAt(LocalDateTime.now());
            rolePermissionMapper.insert(relation);
        }
        auditService.record("SYS_ROLE_PERMISSION_ASSIGN", "分配角色权限", "sys_role", roleId);
        return permissionIds;
    }

    public List<Long> listRolePermissionIds(Long roleId) {
        return rolePermissionMapper.selectList(new LambdaQueryWrapper<SysRolePermissionEntity>().eq(SysRolePermissionEntity::getRoleId, roleId))
                .stream().map(SysRolePermissionEntity::getPermissionId).toList();
    }

    private SysOrgEntity requireOrg(Long id) {
        SysOrgEntity value = orgMapper.selectById(id);
        if (value == null) throw new IllegalArgumentException("组织不存在: " + id);
        return value;
    }

    private SysUserEntity requireUser(Long id) {
        SysUserEntity value = userMapper.selectById(id);
        if (value == null) throw new IllegalArgumentException("用户不存在: " + id);
        return value;
    }

    private SysRoleEntity requireRole(Long id) {
        SysRoleEntity value = roleMapper.selectById(id);
        if (value == null) throw new IllegalArgumentException("角色不存在: " + id);
        return value;
    }

    private SysPermissionEntity requirePermission(Long id) {
        SysPermissionEntity value = permissionMapper.selectById(id);
        if (value == null) throw new IllegalArgumentException("权限不存在: " + id);
        return value;
    }

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
