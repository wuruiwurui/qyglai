package com.qyglai.automation.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 组织保存请求。
 */
public record SystemOrgSaveRequest(
        Long parentId,
        @NotBlank(message = "orgCode is required") String orgCode,
        @NotBlank(message = "orgName is required") String orgName,
        String orgType,
        Integer sortOrder,
        String status
) {
}
