package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 对账批次实体。
 */
@Data
@TableName("reconciliation_batch")
public class ReconciliationBatchEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 批次编号。
     */
    @TableField("batch_no")
    private String batchNo;

    /**
     * 供应商名称。
     */
    @TableField("supplier_name")
    private String supplierName;

    /**
     * 账期开始日期。
     */
    @TableField("period_start")
    private LocalDate periodStart;

    /**
     * 账期结束日期。
     */
    @TableField("period_end")
    private LocalDate periodEnd;

    /**
     * 应有金额。
     */
    @TableField("expected_amount")
    private BigDecimal expectedAmount;

    /**
     * 实际金额。
     */
    @TableField("actual_amount")
    private BigDecimal actualAmount;

    /**
     * 差异金额。
     */
    @TableField("diff_amount")
    private BigDecimal diffAmount;

    /**
     * 状态。
     */
    private String status;

    /**
     * 归属用户ID。
     */
    @TableField("owner_user_id")
    private Long ownerUserId;

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
