package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 客服工单实体。
 */
@Data
@TableName("ticket")
public class TicketEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 工单编号。
     */
    @TableField("ticket_no")
    private String ticketNo;

    /**
     * 客户名称。
     */
    @TableField("customer_name")
    private String customerName;

    /**
     * 来源渠道。
     */
    @TableField("source_channel")
    private String sourceChannel;

    /**
     * 标题。
     */
    private String title;

    /**
     * 工单内容。
     */
    private String content;

    /**
     * 分类。
     */
    private String category;

    /**
     * 优先级。
     */
    private String priority;

    /**
     * 情绪倾向。
     */
    private String sentiment;

    /**
     * SLA截止时间。
     */
    @TableField("sla_deadline")
    private LocalDateTime slaDeadline;

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
     * 置信度。
     */
    private BigDecimal confidence;

    /**
     * 逻辑删除标识。
     */
    @TableLogic
    private Integer deleted;

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
