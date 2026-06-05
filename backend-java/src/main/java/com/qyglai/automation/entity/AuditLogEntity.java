package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 审计日志实体。
 */
@Data
@TableName("audit_log")
public class AuditLogEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 操作人用户ID。
     */
    @TableField("operator_user_id")
    private Long operatorUserId;

    /**
     * 操作人名称。
     */
    @TableField("operator_name")
    private String operatorName;

    /**
     * 操作编码。
     */
    @TableField("action_code")
    private String actionCode;

    /**
     * 操作名称。
     */
    @TableField("action_name")
    private String actionName;

    /**
     * 目标类型。
     */
    @TableField("target_type")
    private String targetType;

    /**
     * 目标ID。
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 请求IP。
     */
    @TableField("request_ip")
    private String requestIp;

    /**
     * 用户代理。
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 变更前JSON。
     */
    @TableField("before_json")
    private String beforeJson;

    /**
     * 变更后JSON。
     */
    @TableField("after_json")
    private String afterJson;

    /**
     * 创建时间。
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

}
