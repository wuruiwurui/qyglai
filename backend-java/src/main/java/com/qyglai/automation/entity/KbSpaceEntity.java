package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 知识库空间实体。
 */
@Data
@TableName("kb_space")
public class KbSpaceEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 知识库空间编码。
     */
    @TableField("space_code")
    private String spaceCode;

    /**
     * 知识库空间名称。
     */
    @TableField("space_name")
    private String spaceName;

    /**
     * 权限范围。
     */
    @TableField("permission_scope")
    private String permissionScope;

    /**
     * 归属组织ID。
     */
    @TableField("owner_org_id")
    private Long ownerOrgId;

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
