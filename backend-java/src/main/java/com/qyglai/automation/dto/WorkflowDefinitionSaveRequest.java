package com.qyglai.automation.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 流程定义保存请求。
 *
 * @param workflowCode 流程编码
 * @param workflowName 流程名称
 * @param scenario 业务场景
 * @param definitionJson 流程节点定义JSON
 * @param status 状态
 */
public record WorkflowDefinitionSaveRequest(
        @NotBlank(message = "workflowCode is required")
        String workflowCode,
        @NotBlank(message = "workflowName is required")
        String workflowName,
        @NotBlank(message = "scenario is required")
        String scenario,
        @NotBlank(message = "definitionJson is required")
        String definitionJson,
        String status
) {
}
