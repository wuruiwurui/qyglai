package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 审批动作日志实体，用于保存不可变的流程审计轨迹。
 */
@Data
@TableName("workflow_action_log")
public class WorkflowActionLogEntity {

    /** 主键ID。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 流程实例ID。 */
    @TableField("instance_id")
    private Long instanceId;

    /** 流程任务ID。 */
    @TableField("task_id")
    private Long taskId;

    /** 节点编码。 */
    @TableField("node_code")
    private String nodeCode;

    /** 动作类型，例如发起、通过、驳回、转交。 */
    private String action;

    /** 操作人用户ID。 */
    @TableField("operator_user_id")
    private Long operatorUserId;

    /** 转交等动作的目标用户ID。 */
    @TableField("target_user_id")
    private Long targetUserId;

    /** 审批意见。 */
    private String comment;

    /** 动作发生时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
