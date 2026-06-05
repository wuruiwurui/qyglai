package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Webhook事件实体。
 */
@Data
@TableName("webhook_event")
public class WebhookEventEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 连接器ID。
     */
    @TableField("connector_id")
    private Long connectorId;

    /**
     * 事件类型。
     */
    @TableField("event_type")
    private String eventType;

    /**
     * 事件键。
     */
    @TableField("event_key")
    private String eventKey;

    /**
     * 事件载荷JSON。
     */
    @TableField("payload_json")
    private String payloadJson;

    /**
     * 处理状态。
     */
    @TableField("process_status")
    private String processStatus;

    /**
     * 错误信息。
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 接收时间。
     */
    @TableField("received_at")
    private LocalDateTime receivedAt;

    /**
     * 处理时间。
     */
    @TableField("processed_at")
    private LocalDateTime processedAt;

}
