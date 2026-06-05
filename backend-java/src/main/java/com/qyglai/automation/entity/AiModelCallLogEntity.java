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
 * AI模型调用日志实体。
 */
@Data
@TableName("ai_model_call_log")
public class AiModelCallLogEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 模型供应商ID。
     */
    @TableField("provider_id")
    private Long providerId;

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
     * 模型名称。
     */
    @TableField("model_name")
    private String modelName;

    /**
     * 提示词模板编码。
     */
    @TableField("prompt_template_code")
    private String promptTemplateCode;

    /**
     * 请求Token数。
     */
    @TableField("request_tokens")
    private Integer requestTokens;

    /**
     * 响应Token数。
     */
    @TableField("response_tokens")
    private Integer responseTokens;

    /**
     * 调用成本金额。
     */
    @TableField("cost_amount")
    private BigDecimal costAmount;

    /**
     * 调用耗时毫秒。
     */
    @TableField("latency_ms")
    private Integer latencyMs;

    /**
     * 调用成功标识。
     */
    @TableField("success_flag")
    private Integer successFlag;

    /**
     * 错误信息。
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 请求哈希。
     */
    @TableField("request_hash")
    private String requestHash;

    /**
     * 创建时间。
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

}
