package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 流程定义实体。
 */
@Data
@TableName("workflow_definition")
public class WorkflowDefinitionEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 流程编码。
     */
    @TableField("workflow_code")
    private String workflowCode;

    /**
     * 流程名称。
     */
    @TableField("workflow_name")
    private String workflowName;

    /**
     * 业务场景。
     */
    private String scenario;

    /**
     * 版本号。
     */
    @TableField("version_no")
    private Integer versionNo;

    /**
     * 流程定义JSON。
     */
    @TableField("definition_json")
    private String definitionJson;

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
