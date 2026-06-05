package com.qyglai.automation.controller;

import java.util.List;

import com.qyglai.automation.common.ApiResponse;
import com.qyglai.automation.dto.SimpleCreateRequest;
import com.qyglai.automation.dto.IdAssignmentRequest;
import com.qyglai.automation.dto.SystemOrgSaveRequest;
import com.qyglai.automation.dto.SystemPermissionSaveRequest;
import com.qyglai.automation.dto.SystemRoleSaveRequest;
import com.qyglai.automation.dto.SystemUserSaveRequest;
import com.qyglai.automation.entity.SysOrgEntity;
import com.qyglai.automation.entity.SysPermissionEntity;
import com.qyglai.automation.entity.SysRoleEntity;
import com.qyglai.automation.entity.SysUserEntity;
import com.qyglai.automation.service.SystemManagementService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 系统管理接口。
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/system")
@PreAuthorize("hasAnyAuthority('*', 'system:manage')")
public class SystemManagementController {

    private final SystemManagementService service;

    public SystemManagementController(SystemManagementService service) {
        this.service = service;
    }

    /**
     * 创建组织。
     *
     * @param request 创建请求
     * @return 组织实体
     */
    @PostMapping("/orgs")
    public ApiResponse<SysOrgEntity> createOrg(@RequestBody SimpleCreateRequest request) {
        return ApiResponse.ok(service.createOrg(request));
    }

    /**
     * 查询组织列表。
     *
     * @return 组织列表
     */
    @GetMapping("/orgs")
    public ApiResponse<List<SysOrgEntity>> orgs() {
        return ApiResponse.ok(service.listOrgs());
    }

    /**
     * 查询用户列表。
     *
     * @return 用户列表
     */
    @GetMapping("/users")
    public ApiResponse<List<SysUserEntity>> users() {
        return ApiResponse.ok(service.listUsers());
    }

    /**
     * 查询角色列表。
     *
     * @return 角色列表
     */
    @GetMapping("/roles")
    public ApiResponse<List<SysRoleEntity>> roles() {
        return ApiResponse.ok(service.listRoles());
    }

    /**
     * 查询权限列表。
     *
     * @return 权限列表
     */
    @GetMapping("/permissions")
    public ApiResponse<List<SysPermissionEntity>> permissions() {
        return ApiResponse.ok(service.listPermissions());
    }

    @PostMapping("/orgs/manage")
    public ApiResponse<SysOrgEntity> saveOrg(@Valid @RequestBody SystemOrgSaveRequest request) {
        return ApiResponse.ok(service.saveOrg(null, request));
    }

    @PutMapping("/orgs/{id}")
    public ApiResponse<SysOrgEntity> updateOrg(@PathVariable Long id, @Valid @RequestBody SystemOrgSaveRequest request) {
        return ApiResponse.ok(service.saveOrg(id, request));
    }

    @PostMapping("/users")
    public ApiResponse<SysUserEntity> saveUser(@Valid @RequestBody SystemUserSaveRequest request) {
        return ApiResponse.ok(service.saveUser(null, request));
    }

    @PutMapping("/users/{id}")
    public ApiResponse<SysUserEntity> updateUser(@PathVariable Long id, @Valid @RequestBody SystemUserSaveRequest request) {
        return ApiResponse.ok(service.saveUser(id, request));
    }

    @PostMapping("/roles")
    public ApiResponse<SysRoleEntity> saveRole(@Valid @RequestBody SystemRoleSaveRequest request) {
        return ApiResponse.ok(service.saveRole(null, request));
    }

    @PutMapping("/roles/{id}")
    public ApiResponse<SysRoleEntity> updateRole(@PathVariable Long id, @Valid @RequestBody SystemRoleSaveRequest request) {
        return ApiResponse.ok(service.saveRole(id, request));
    }

    @PostMapping("/permissions")
    public ApiResponse<SysPermissionEntity> savePermission(@Valid @RequestBody SystemPermissionSaveRequest request) {
        return ApiResponse.ok(service.savePermission(null, request));
    }

    @PutMapping("/permissions/{id}")
    public ApiResponse<SysPermissionEntity> updatePermission(@PathVariable Long id, @Valid @RequestBody SystemPermissionSaveRequest request) {
        return ApiResponse.ok(service.savePermission(id, request));
    }

    @GetMapping("/users/{id}/role-ids")
    public ApiResponse<List<Long>> userRoleIds(@PathVariable Long id) {
        return ApiResponse.ok(service.listUserRoleIds(id));
    }

    @PutMapping("/users/{id}/role-ids")
    public ApiResponse<List<Long>> assignUserRoles(@PathVariable Long id, @Valid @RequestBody IdAssignmentRequest request) {
        return ApiResponse.ok(service.assignUserRoles(id, request.ids()));
    }

    @GetMapping("/roles/{id}/permission-ids")
    public ApiResponse<List<Long>> rolePermissionIds(@PathVariable Long id) {
        return ApiResponse.ok(service.listRolePermissionIds(id));
    }

    @PutMapping("/roles/{id}/permission-ids")
    public ApiResponse<List<Long>> assignRolePermissions(@PathVariable Long id, @Valid @RequestBody IdAssignmentRequest request) {
        return ApiResponse.ok(service.assignRolePermissions(id, request.ids()));
    }
}
