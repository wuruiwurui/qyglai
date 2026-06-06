USE qyglai;

CREATE TABLE IF NOT EXISTS workflow_action_log (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  instance_id BIGINT NOT NULL COMMENT '流程实例ID',
  task_id BIGINT NULL COMMENT '流程任务ID',
  node_code VARCHAR(128) NOT NULL COMMENT '节点编码',
  action VARCHAR(32) NOT NULL COMMENT '审批动作：start发起、arrive到达、approved通过、rejected驳回、transfer转交、complete完成',
  operator_user_id BIGINT NULL COMMENT '操作人用户ID',
  target_user_id BIGINT NULL COMMENT '目标用户ID',
  comment VARCHAR(1000) NULL COMMENT '审批意见',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '动作发生时间',
  KEY idx_workflow_action_instance (instance_id, created_at),
  KEY idx_workflow_action_task (task_id),
  CONSTRAINT fk_workflow_action_instance FOREIGN KEY (instance_id) REFERENCES workflow_instance (id),
  CONSTRAINT fk_workflow_action_task FOREIGN KEY (task_id) REFERENCES workflow_task (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审批动作历史表';

INSERT INTO workflow_definition
  (id, workflow_code, workflow_name, scenario, version_no, definition_json, status, deleted)
SELECT
  910000000000000001, 'contract_approval', '合同审批流程', 'contract', 1,
  JSON_OBJECT('nodes', JSON_ARRAY(
    JSON_OBJECT('code', 'department_review', 'name', '部门负责人审批', 'dueHours', 24),
    JSON_OBJECT('code', 'legal_review', 'name', '法务审批', 'dueHours', 24),
    JSON_OBJECT('code', 'final_review', 'name', '总经理审批', 'dueHours', 48)
  )), 'enabled', 0
WHERE NOT EXISTS (SELECT 1 FROM workflow_definition WHERE workflow_code = 'contract_approval');

INSERT INTO workflow_definition
  (id, workflow_code, workflow_name, scenario, version_no, definition_json, status, deleted)
SELECT
  910000000000000002, 'invoice_approval', '发票报销审批流程', 'invoice', 1,
  JSON_OBJECT('nodes', JSON_ARRAY(
    JSON_OBJECT('code', 'business_review', 'name', '业务负责人审批', 'dueHours', 24),
    JSON_OBJECT('code', 'finance_review', 'name', '财务审批', 'dueHours', 24)
  )), 'enabled', 0
WHERE NOT EXISTS (SELECT 1 FROM workflow_definition WHERE workflow_code = 'invoice_approval');

INSERT INTO workflow_definition
  (id, workflow_code, workflow_name, scenario, version_no, definition_json, status, deleted)
SELECT
  910000000000000003, 'payment_approval', '付款审批流程', 'payment', 1,
  JSON_OBJECT('nodes', JSON_ARRAY(
    JSON_OBJECT('code', 'department_review', 'name', '部门审批', 'dueHours', 24),
    JSON_OBJECT('code', 'finance_review', 'name', '财务复核', 'dueHours', 24),
    JSON_OBJECT('code', 'manager_review', 'name', '管理层审批', 'dueHours', 48)
  )), 'enabled', 0
WHERE NOT EXISTS (SELECT 1 FROM workflow_definition WHERE workflow_code = 'payment_approval');
