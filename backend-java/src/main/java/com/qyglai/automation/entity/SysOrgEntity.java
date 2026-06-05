package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 组织架构实体。
 */
@Data
@TableName("sys_org")
public class SysOrgEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 父级ID。
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 组织编码。
     */
    @TableField("org_code")
    private String orgCode;

    /**
     * 组织名称。
     */
    @TableField("org_name")
    private String orgName;

    /**
     * 组织类型。
     */
    @TableField("org_type")
    private String orgType;

    /**
     * 排序号。
     */
    @TableField("sort_order")
    private Integer sortOrder;

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
