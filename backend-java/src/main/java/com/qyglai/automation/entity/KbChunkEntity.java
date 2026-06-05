package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 知识库切片实体。
 */
@Data
@TableName("kb_chunk")
public class KbChunkEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 文档ID。
     */
    @TableField("document_id")
    private Long documentId;

    /**
     * 切片序号。
     */
    @TableField("chunk_index")
    private Integer chunkIndex;

    /**
     * 知识切片内容。
     */
    private String content;

    /**
     * Token数量。
     */
    @TableField("token_count")
    private Integer tokenCount;

    /**
     * 向量模型名称。
     */
    @TableField("embedding_model")
    private String embeddingModel;

    /**
     * 向量存储引用。
     */
    @TableField("vector_ref")
    private String vectorRef;

    /**
     * 元数据JSON。
     */
    @TableField("metadata_json")
    private String metadataJson;

    /**
     * 创建时间。
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

}
