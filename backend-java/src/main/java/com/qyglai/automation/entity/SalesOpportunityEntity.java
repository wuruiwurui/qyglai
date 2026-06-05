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
 * 销售商机实体。
 */
@Data
@TableName("sales_opportunity")
public class SalesOpportunityEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 客户ID。
     */
    @TableField("customer_id")
    private Long customerId;

    /**
     * 商机名称。
     */
    @TableField("opportunity_name")
    private String opportunityName;

    /**
     * 销售阶段。
     */
    private String stage;

    /**
     * 预计金额。
     */
    @TableField("expected_amount")
    private BigDecimal expectedAmount;

    /**
     * 预计成交日期。
     */
    @TableField("expected_close_date")
    private LocalDate expectedCloseDate;

    /**
     * 赢单概率。
     */
    @TableField("win_probability")
    private BigDecimal winProbability;

    /**
     * 归属用户ID。
     */
    @TableField("owner_user_id")
    private Long ownerUserId;

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
