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
 * 对账明细实体。
 */
@Data
@TableName("reconciliation_item")
public class ReconciliationItemEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 批次ID。
     */
    @TableField("batch_id")
    private Long batchId;

    /**
     * 发票ID。
     */
    @TableField("invoice_id")
    private Long invoiceId;

    /**
     * 明细类型。
     */
    @TableField("item_type")
    private String itemType;

    /**
     * 来源单号。
     */
    @TableField("source_no")
    private String sourceNo;

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
     * 差异原因。
     */
    @TableField("diff_reason")
    private String diffReason;

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
