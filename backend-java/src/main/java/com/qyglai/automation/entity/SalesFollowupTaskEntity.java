package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 销售跟进任务实体。
 */
@Data
@TableName("sales_followup_task")
public class SalesFollowupTaskEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 商机ID。
     */
    @TableField("opportunity_id")
    private Long opportunityId;

    /**
     * 客户ID。
     */
    @TableField("customer_id")
    private Long customerId;

    /**
     * 任务标题。
     */
    @TableField("task_title")
    private String taskTitle;

    /**
     * 下一步动作。
     */
    @TableField("next_action")
    private String nextAction;

    /**
     * 截止时间。
     */
    @TableField("due_time")
    private LocalDateTime dueTime;

    /**
     * 归属用户ID。
     */
    @TableField("owner_user_id")
    private Long ownerUserId;

    /**
     * 状态。
     */
    private String status;

    /**
     * 来源类型。
     */
    @TableField("source_type")
    private String sourceType;

    /**
     * 来源引用ID。
     */
    @TableField("source_ref_id")
    private Long sourceRefId;

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
