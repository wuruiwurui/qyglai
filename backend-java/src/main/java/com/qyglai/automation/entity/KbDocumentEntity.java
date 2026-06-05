package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 知识库文档实体。
 */
@Data
@TableName("kb_document")
public class KbDocumentEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 知识库空间ID。
     */
    @TableField("space_id")
    private Long spaceId;

    /**
     * 文件ID。
     */
    @TableField("file_id")
    private Long fileId;

    /**
     * 标题。
     */
    private String title;

    /**
     * 文档类型。
     */
    @TableField("doc_type")
    private String docType;

    /**
     * 来源URL。
     */
    @TableField("source_url")
    private String sourceUrl;

    /**
     * 版本号。
     */
    @TableField("version_no")
    private String versionNo;

    /**
     * 索引状态。
     */
    @TableField("indexing_status")
    private String indexingStatus;

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
