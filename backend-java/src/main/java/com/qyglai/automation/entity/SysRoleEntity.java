package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 系统角色实体。
 */
@Data
@TableName("sys_role")
public class SysRoleEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 角色编码。
     */
    @TableField("role_code")
    private String roleCode;

    /**
     * 角色名称。
     */
    @TableField("role_name")
    private String roleName;

    /**
     * 数据权限范围。
     */
    @TableField("data_scope")
    private String dataScope;

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
