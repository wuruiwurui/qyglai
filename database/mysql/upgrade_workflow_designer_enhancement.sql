-- 工作流设计器增强：企业共享节点组件库
CREATE TABLE IF NOT EXISTS workflow_node_template (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  template_key VARCHAR(128) NOT NULL COMMENT '组件唯一编码',
  node_type VARCHAR(32) NOT NULL COMMENT '节点执行类型：approval或ai',
  label VARCHAR(128) NOT NULL COMMENT '组件名称',
  description VARCHAR(500) NULL COMMENT '组件说明',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_workflow_node_template_key(template_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流共享节点组件表';
