package com.qyglai.automation.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 工作流共享节点组件保存请求。
 *
 * @param templateKey 组件唯一编码
 * @param nodeType 执行类型
 * @param label 组件名称
 * @param description 组件说明
 */
public record WorkflowNodeTemplateSaveRequest(
        String templateKey,
        @NotBlank(message = "nodeType is required") String nodeType,
        @NotBlank(message = "label is required") String label,
        String description
) {
}
