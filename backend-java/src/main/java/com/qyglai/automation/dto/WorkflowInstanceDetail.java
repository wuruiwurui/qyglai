package com.qyglai.automation.dto;

import com.qyglai.automation.entity.WorkflowActionLogEntity;
import com.qyglai.automation.entity.WorkflowDefinitionEntity;
import com.qyglai.automation.entity.WorkflowInstanceEntity;
import com.qyglai.automation.entity.WorkflowTaskEntity;
import java.util.List;

/**
 * 流程实例完整详情。
 *
 * @param instance 流程实例
 * @param definition 流程定义
 * @param tasks 全部节点任务
 * @param history 审批历史
 */
public record WorkflowInstanceDetail(
        WorkflowInstanceEntity instance,
        WorkflowDefinitionEntity definition,
        List<WorkflowTaskEntity> tasks,
        List<WorkflowActionLogEntity> history
) {
}
