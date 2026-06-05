package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 合同风险项实体。
 */
@Data
@TableName("contract_risk_item")
public class ContractRiskItemEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 合同ID。
     */
    @TableField("contract_id")
    private Long contractId;

    /**
     * 风险编码。
     */
    @TableField("risk_code")
    private String riskCode;

    /**
     * 风险名称。
     */
    @TableField("risk_name")
    private String riskName;

    /**
     * 风险等级。
     */
    @TableField("risk_level")
    private String riskLevel;

    /**
     * 风险依据。
     */
    private String evidence;

    /**
     * 处理建议。
     */
    private String suggestion;

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
