package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 销售客户实体。
 */
@Data
@TableName("sales_customer")
public class SalesCustomerEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 客户名称。
     */
    @TableField("customer_name")
    private String customerName;

    /**
     * 行业。
     */
    private String industry;

    /**
     * 地区。
     */
    private String region;

    /**
     * 联系人姓名。
     */
    @TableField("contact_name")
    private String contactName;

    /**
     * 联系人手机号。
     */
    @TableField("contact_mobile")
    private String contactMobile;

    /**
     * 归属用户ID。
     */
    @TableField("owner_user_id")
    private Long ownerUserId;

    /**
     * 客户等级。
     */
    @TableField("customer_level")
    private String customerLevel;

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
