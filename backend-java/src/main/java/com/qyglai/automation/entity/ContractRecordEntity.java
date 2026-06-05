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
 * 合同台账实体。
 */
@Data
@TableName("contract_record")
public class ContractRecordEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 合同编号。
     */
    @TableField("contract_no")
    private String contractNo;

    /**
     * 文件ID。
     */
    @TableField("file_id")
    private Long fileId;

    /**
     * 甲方名称。
     */
    @TableField("party_a")
    private String partyA;

    /**
     * 乙方名称。
     */
    @TableField("party_b")
    private String partyB;

    /**
     * 合同金额。
     */
    private BigDecimal amount;

    /**
     * 币种。
     */
    private String currency;

    /**
     * 开始日期。
     */
    @TableField("start_date")
    private LocalDate startDate;

    /**
     * 结束日期。
     */
    @TableField("end_date")
    private LocalDate endDate;

    /**
     * 签署日期。
     */
    @TableField("sign_date")
    private LocalDate signDate;

    /**
     * 付款条款。
     */
    @TableField("payment_terms")
    private String paymentTerms;

    /**
     * 开票条款。
     */
    @TableField("invoice_terms")
    private String invoiceTerms;

    /**
     * 续约条款。
     */
    @TableField("renewal_terms")
    private String renewalTerms;

    /**
     * 风险等级。
     */
    @TableField("risk_level")
    private String riskLevel;

    /**
     * 复核状态。
     */
    @TableField("review_status")
    private String reviewStatus;

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
