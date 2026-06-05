package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 流程任务实体。
 */
@Data
@TableName("workflow_task")
public class WorkflowTaskEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 流程实例ID。
     */
    @TableField("instance_id")
    private Long instanceId;

    /**
     * 节点编码。
     */
    @TableField("node_code")
    private String nodeCode;

    /**
     * 节点名称。
     */
    @TableField("node_name")
    private String nodeName;

    /**
     * 处理人用户ID。
     */
    @TableField("assignee_user_id")
    private Long assigneeUserId;

    /**
     * 任务类型。
     */
    @TableField("task_type")
    private String taskType;

    /**
     * 状态。
     */
    private String status;

    /**
     * 截止时间。
     */
    @TableField("due_time")
    private LocalDateTime dueTime;

    /**
     * 完成时间。
     */
    @TableField("completed_at")
    private LocalDateTime completedAt;

    /**
     * 处理结果JSON。
     */
    @TableField("result_json")
    private String resultJson;

    /**
     * 创建时间。
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
