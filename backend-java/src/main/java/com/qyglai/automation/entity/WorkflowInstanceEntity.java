package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 流程实例实体。
 */
@Data
@TableName("workflow_instance")
public class WorkflowInstanceEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 流程定义ID。
     */
    @TableField("definition_id")
    private Long definitionId;

    /**
     * 业务类型。
     */
    @TableField("business_type")
    private String businessType;

    /**
     * 业务ID。
     */
    @TableField("business_id")
    private Long businessId;

    /**
     * 发起人用户ID。
     */
    @TableField("initiator_user_id")
    private Long initiatorUserId;

    /**
     * 当前节点。
     */
    @TableField("current_node")
    private String currentNode;

    /**
     * 流程变量JSON。
     */
    @TableField("variables_json")
    private String variablesJson;

    /**
     * 状态。
     */
    private String status;

    /**
     * 开始时间。
     */
    @TableField("started_at")
    private LocalDateTime startedAt;

    /**
     * 结束时间。
     */
    @TableField("ended_at")
    private LocalDateTime endedAt;

    /**
     * 更新时间。
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
