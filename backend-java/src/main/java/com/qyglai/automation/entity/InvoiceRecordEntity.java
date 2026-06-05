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
 * 发票记录实体。
 */
@Data
@TableName("invoice_record")
public class InvoiceRecordEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 发票号码。
     */
    @TableField("invoice_no")
    private String invoiceNo;

    /**
     * 发票代码。
     */
    @TableField("invoice_code")
    private String invoiceCode;

    /**
     * 文件ID。
     */
    @TableField("file_id")
    private Long fileId;

    /**
     * 购买方名称。
     */
    @TableField("buyer_name")
    private String buyerName;

    /**
     * 购买方税号。
     */
    @TableField("buyer_tax_no")
    private String buyerTaxNo;

    /**
     * 销售方名称。
     */
    @TableField("seller_name")
    private String sellerName;

    /**
     * 销售方税号。
     */
    @TableField("seller_tax_no")
    private String sellerTaxNo;

    /**
     * 开票日期。
     */
    @TableField("invoice_date")
    private LocalDate invoiceDate;

    /**
     * 不含税金额。
     */
    private BigDecimal amount;

    /**
     * 税额。
     */
    @TableField("tax_amount")
    private BigDecimal taxAmount;

    /**
     * 价税合计金额。
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 税率。
     */
    @TableField("tax_rate")
    private String taxRate;

    /**
     * 校验状态。
     */
    @TableField("verify_status")
    private String verifyStatus;

    /**
     * 重复发票标识。
     */
    @TableField("duplicate_flag")
    private Integer duplicateFlag;

    /**
     * 归属用户ID。
     */
    @TableField("owner_user_id")
    private Long ownerUserId;

    /**
     * 组织ID。
     */
    @TableField("org_id")
    private Long orgId;

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
