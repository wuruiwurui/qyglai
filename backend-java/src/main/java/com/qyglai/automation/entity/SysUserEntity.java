package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 系统用户实体。
 */
@Data
@TableName("sys_user")
public class SysUserEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 组织ID。
     */
    @TableField("org_id")
    private Long orgId;

    /**
     * 登录用户名。
     */
    private String username;

    /**
     * 真实姓名。
     */
    @TableField("real_name")
    private String realName;

    /**
     * 密码哈希。
     */
    @TableField("password_hash")
    private String passwordHash;

    /**
     * 手机号。
     */
    private String mobile;

    /**
     * 邮箱。
     */
    private String email;

    /**
     * 头像地址。
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 状态。
     */
    private String status;

    /**
     * 最后登录时间。
     */
    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

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
