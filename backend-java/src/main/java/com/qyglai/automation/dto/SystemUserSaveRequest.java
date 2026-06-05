package com.qyglai.automation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 系统用户保存请求。
 */
public record SystemUserSaveRequest(
        @NotNull(message = "orgId is required") Long orgId,
        @NotBlank(message = "username is required") String username,
        @NotBlank(message = "realName is required") String realName,
        String password,
        String mobile,
        String email,
        String status
) {
}
