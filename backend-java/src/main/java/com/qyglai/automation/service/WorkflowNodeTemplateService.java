package com.qyglai.automation.service;

import com.qyglai.automation.dto.WorkflowNodeTemplateSaveRequest;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 工作流共享节点组件库服务。
 */
@Service
public class WorkflowNodeTemplateService {

    private final JdbcTemplate jdbcTemplate;

    public WorkflowNodeTemplateService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        ensureTable();
    }

    /** 查询企业共享的自定义节点组件。 */
    public List<Map<String, Object>> list() {
        return jdbcTemplate.queryForList("""
                SELECT id, template_key AS templateKey, node_type AS nodeType, label, description,
                       created_at AS createdAt, updated_at AS updatedAt
                FROM workflow_node_template WHERE deleted=0 ORDER BY created_at
                """);
    }

    /** 新增共享节点组件。 */
    public Map<String, Object> save(WorkflowNodeTemplateSaveRequest request) {
        String key = request.templateKey() == null || request.templateKey().isBlank()
                ? "custom_" + System.currentTimeMillis() : request.templateKey().trim();
        jdbcTemplate.update("""
                INSERT INTO workflow_node_template(template_key,node_type,label,description)
                VALUES(?,?,?,?)
                ON DUPLICATE KEY UPDATE node_type=VALUES(node_type),label=VALUES(label),
                  description=VALUES(description),deleted=0
                """, key, request.nodeType(), request.label(), request.description());
        return jdbcTemplate.queryForMap("""
                SELECT id, template_key AS templateKey, node_type AS nodeType, label, description
                FROM workflow_node_template WHERE template_key=?
                """, key);
    }

    /** 删除共享节点组件，不影响已经发布的流程定义。 */
    public void delete(String key) {
        jdbcTemplate.update("UPDATE workflow_node_template SET deleted=1 WHERE template_key=?", key);
    }

    private void ensureTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS workflow_node_template (
                  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
                  template_key VARCHAR(128) NOT NULL COMMENT '组件唯一编码',
                  node_type VARCHAR(32) NOT NULL COMMENT '节点执行类型',
                  label VARCHAR(128) NOT NULL COMMENT '组件名称',
                  description VARCHAR(500) NULL COMMENT '组件说明',
                  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
                  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                  UNIQUE KEY uk_workflow_node_template_key(template_key)
                ) COMMENT='工作流共享节点组件表'
                """);
    }
}
