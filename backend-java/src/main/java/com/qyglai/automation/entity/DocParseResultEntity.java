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
 * 文档解析结果实体。
 */
@Data
@TableName("doc_parse_result")
public class DocParseResultEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 文件ID。
     */
    @TableField("file_id")
    private Long fileId;

    /**
     * 页码。
     */
    @TableField("page_no")
    private Integer pageNo;

    /**
     * 原始识别文本。
     */
    @TableField("raw_text")
    private String rawText;

    /**
     * 版面解析JSON。
     */
    @TableField("layout_json")
    private String layoutJson;

    /**
     * OCR引擎。
     */
    @TableField("ocr_engine")
    private String ocrEngine;

    /**
     * 置信度。
     */
    private BigDecimal confidence;

    /**
     * 状态。
     */
    private String status;

    /**
     * 创建时间。
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

}
