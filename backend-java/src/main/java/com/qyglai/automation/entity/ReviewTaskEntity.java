package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 人工复核任务实体。
 */
@Data
@TableName("review_task")
public class ReviewTaskEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 任务编号。
     */
    @TableField("task_no")
    private String taskNo;

    /**
     * 业务场景。
     */
    private String scenario;

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
     * 标题。
     */
    private String title;

    /**
     * 风险等级。
     */
    @TableField("risk_level")
    private String riskLevel;

    /**
     * 处理人用户ID。
     */
    @TableField("assignee_user_id")
    private Long assigneeUserId;

    /**
     * 状态。
     */
    private String status;

    /**
     * 复核结果。
     */
    @TableField("review_result")
    private String reviewResult;

    /**
     * 完成时间。
     */
    @TableField("completed_at")
    private LocalDateTime completedAt;

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
