package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 文件资产实体。
 */
@Data
@TableName("file_asset")
public class FileAssetEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 存储文件名。
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 原始文件名。
     */
    @TableField("original_name")
    private String originalName;

    /**
     * 文件扩展名。
     */
    @TableField("file_ext")
    private String fileExt;

    /**
     * 文件MIME类型。
     */
    @TableField("mime_type")
    private String mimeType;

    /**
     * 文件大小，单位字节。
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 文件哈希值。
     */
    @TableField("file_hash")
    private String fileHash;

    /**
     * 对象存储桶名称。
     */
    @TableField("storage_bucket")
    private String storageBucket;

    /**
     * 对象存储键。
     */
    @TableField("storage_key")
    private String storageKey;

    /**
     * 业务类型。
     */
    @TableField("business_type")
    private String businessType;

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
     * 解析状态。
     */
    @TableField("parse_status")
    private String parseStatus;

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
