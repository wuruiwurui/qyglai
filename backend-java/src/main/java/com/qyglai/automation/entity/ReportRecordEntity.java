package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 报表生成记录实体。
 */
@Data
@TableName("report_record")
public class ReportRecordEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 模板ID。
     */
    @TableField("template_id")
    private Long templateId;

    /**
     * 报表类型。
     */
    @TableField("report_type")
    private String reportType;

    /**
     * 标题。
     */
    private String title;

    /**
     * 摘要。
     */
    private String summary;

    /**
     * 报表正文。
     */
    private String content;

    /**
     * 数据来源JSON。
     */
    @TableField("source_json")
    private String sourceJson;

    /**
     * 生成人用户ID。
     */
    @TableField("generated_by")
    private Long generatedBy;

    /**
     * 发送状态。
     */
    @TableField("send_status")
    private String sendStatus;

    /**
     * 生成时间。
     */
    @TableField("generated_at")
    private LocalDateTime generatedAt;

    /**
     * 更新时间。
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
