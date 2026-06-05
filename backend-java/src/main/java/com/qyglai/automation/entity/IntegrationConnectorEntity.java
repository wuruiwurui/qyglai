package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 第三方集成连接器实体。
 */
@Data
@TableName("integration_connector")
public class IntegrationConnectorEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 连接器编码。
     */
    @TableField("connector_code")
    private String connectorCode;

    /**
     * 连接器名称。
     */
    @TableField("connector_name")
    private String connectorName;

    /**
     * 连接器类型。
     */
    @TableField("connector_type")
    private String connectorType;

    /**
     * 认证配置JSON。
     */
    @TableField("auth_config_json")
    private String authConfigJson;

    /**
     * 接口地址。
     */
    @TableField("endpoint_url")
    private String endpointUrl;

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
