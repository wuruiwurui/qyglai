package com.qyglai.automation.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

/**
 * ID集合授权请求。
 *
 * @param ids 需要分配的ID集合
 */
public record IdAssignmentRequest(
        @NotNull(message = "ids is required")
        List<Long> ids
) {
}
