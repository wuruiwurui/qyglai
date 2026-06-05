package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * AI提示词模板实体。
 */
@Data
@TableName("ai_prompt_template")
public class AiPromptTemplateEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 模板编码。
     */
    @TableField("template_code")
    private String templateCode;

    /**
     * 业务场景。
     */
    private String scenario;

    /**
     * 版本号。
     */
    @TableField("version_no")
    private Integer versionNo;

    /**
     * 模型名称。
     */
    @TableField("model_name")
    private String modelName;

    /**
     * 系统提示词。
     */
    @TableField("system_prompt")
    private String systemPrompt;

    /**
     * 用户提示词模板。
     */
    @TableField("user_prompt_template")
    private String userPromptTemplate;

    /**
     * 输出结构JSON。
     */
    @TableField("output_schema_json")
    private String outputSchemaJson;

    /**
     * 状态。
     */
    private String status;

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
