package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 系统权限实体。
 */
@Data
@TableName("sys_permission")
public class SysPermissionEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 权限编码。
     */
    @TableField("permission_code")
    private String permissionCode;

    /**
     * 权限名称。
     */
    @TableField("permission_name")
    private String permissionName;

    /**
     * 权限类型。
     */
    @TableField("permission_type")
    private String permissionType;

    /**
     * 资源路径。
     */
    @TableField("resource_path")
    private String resourcePath;

    /**
     * 父级ID。
     */
    @TableField("parent_id")
    private Long parentId;

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
