package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 合同提醒实体。
 */
@Data
@TableName("contract_reminder")
public class ContractReminderEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 合同ID。
     */
    @TableField("contract_id")
    private Long contractId;

    /**
     * 提醒类型。
     */
    @TableField("reminder_type")
    private String reminderType;

    /**
     * 提醒时间。
     */
    @TableField("reminder_time")
    private LocalDateTime reminderTime;

    /**
     * 接收用户ID。
     */
    @TableField("receiver_user_id")
    private Long receiverUserId;

    /**
     * 状态。
     */
    private String status;

    /**
     * 发送时间。
     */
    @TableField("sent_at")
    private LocalDateTime sentAt;

    /**
     * 创建时间。
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

}
