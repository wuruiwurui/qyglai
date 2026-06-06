package com.qyglai.automation.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 知识库空间创建请求。
 *
 * @param name 空间名称
 * @param code 空间编码
 * @param scope 权限范围
 * @param ownerOrgId 归属组织ID
 */
public record KnowledgeSpaceCreateRequest(
        @NotBlank(message = "name is required") String name,
        String code,
        String scope,
        Long ownerOrgId
) {
}
