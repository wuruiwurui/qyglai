package com.qyglai.automation.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 系统角色保存请求。
 */
public record SystemRoleSaveRequest(
        @NotBlank(message = "roleCode is required") String roleCode,
        @NotBlank(message = "roleName is required") String roleName,
        String dataScope,
        String status
) {
}
