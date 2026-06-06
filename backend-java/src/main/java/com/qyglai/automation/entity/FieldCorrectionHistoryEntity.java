package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 文件抽取字段修正历史实体。
 */
@Data
@TableName("field_correction_history")
public class FieldCorrectionHistoryEntity {

    /** 主键ID。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 同一次保存操作的修正批次ID。 */
    @TableField("batch_id")
    private Long batchId;

    /** 文件资产ID。 */
    @TableField("file_id")
    private Long fileId;

    /** 关联业务类型。 */
    @TableField("business_type")
    private String businessType;

    /** 关联业务记录ID。 */
    @TableField("business_id")
    private Long businessId;

    /** 字段编码。 */
    @TableField("field_key")
    private String fieldKey;

    /** 字段中文名称。 */
    @TableField("field_name")
    private String fieldName;

    /** 修正前字段值。 */
    @TableField("old_value")
    private String oldValue;

    /** 修正后字段值。 */
    @TableField("new_value")
    private String newValue;

    /** 修正原因。 */
    private String reason;

    /** 操作人用户ID。 */
    @TableField("operator_user_id")
    private Long operatorUserId;

    /** 操作人名称。 */
    @TableField("operator_name")
    private String operatorName;

    /** 修正时间。 */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
