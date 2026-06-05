package com.qyglai.automation.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 系统权限保存请求。
 */
public record SystemPermissionSaveRequest(
        @NotBlank(message = "permissionCode is required") String permissionCode,
        @NotBlank(message = "permissionName is required") String permissionName,
        String permissionType,
        String resourcePath,
        Long parentId,
        Integer sortOrder,
        String status
) {
}
