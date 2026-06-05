package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 报表模板实体。
 */
@Data
@TableName("report_template")
public class ReportTemplateEntity {

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
     * 模板名称。
     */
    @TableField("template_name")
    private String templateName;

    /**
     * 报表类型。
     */
    @TableField("report_type")
    private String reportType;

    /**
     * 模板内容。
     */
    @TableField("template_content")
    private String templateContent;

    /**
     * 目标受众。
     */
    private String audience;

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
