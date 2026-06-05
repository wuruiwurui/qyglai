package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * AI模型供应商实体。
 */
@Data
@TableName("ai_model_provider")
public class AiModelProviderEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 模型供应商编码。
     */
    @TableField("provider_code")
    private String providerCode;

    /**
     * 模型供应商名称。
     */
    @TableField("provider_name")
    private String providerName;

    /**
     * 模型供应商类型。
     */
    @TableField("provider_type")
    private String providerType;

    /**
     * 基础调用地址。
     */
    @TableField("base_url")
    private String baseUrl;

    /**
     * 默认模型。
     */
    @TableField("default_model")
    private String defaultModel;

    /**
     * 状态。
     */
    private String status;

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
