package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 消息通知实体。
 */
@Data
@TableName("message_notification")
public class MessageNotificationEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 消息渠道。
     */
    private String channel;

    /**
     * 接收用户ID。
     */
    @TableField("receiver_user_id")
    private Long receiverUserId;

    /**
     * 接收地址。
     */
    @TableField("receiver_address")
    private String receiverAddress;

    /**
     * 标题。
     */
    private String title;

    /**
     * 消息内容。
     */
    private String content;

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
     * 发送状态。
     */
    @TableField("send_status")
    private String sendStatus;

    /**
     * 重试次数。
     */
    @TableField("retry_count")
    private Integer retryCount;

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

    /**
     * 更新时间。
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
