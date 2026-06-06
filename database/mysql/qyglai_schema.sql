-- QYGL AI enterprise process automation schema
-- Database: MySQL 8.x
-- Charset: utf8mb4

CREATE DATABASE IF NOT EXISTS qyglai
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE qyglai;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS ai_evaluation_sample;
DROP TABLE IF EXISTS ai_model_call_log;
DROP TABLE IF EXISTS ai_prompt_template;
DROP TABLE IF EXISTS ai_model_provider;
DROP TABLE IF EXISTS webhook_event;
DROP TABLE IF EXISTS integration_connector;
DROP TABLE IF EXISTS message_notification;
DROP TABLE IF EXISTS review_task;
DROP TABLE IF EXISTS workflow_task;
DROP TABLE IF EXISTS workflow_instance;
DROP TABLE IF EXISTS workflow_definition;
DROP TABLE IF EXISTS report_record;
DROP TABLE IF EXISTS report_template;
DROP TABLE IF EXISTS kb_chunk;
DROP TABLE IF EXISTS kb_document;
DROP TABLE IF EXISTS kb_space;
DROP TABLE IF EXISTS sales_followup_task;
DROP TABLE IF EXISTS sales_opportunity;
DROP TABLE IF EXISTS sales_customer;
DROP TABLE IF EXISTS ticket_reply_suggestion;
DROP TABLE IF EXISTS ticket;
DROP TABLE IF EXISTS reconciliation_item;
DROP TABLE IF EXISTS reconciliation_batch;
DROP TABLE IF EXISTS invoice_record;
DROP TABLE IF EXISTS contract_reminder;
DROP TABLE IF EXISTS contract_risk_item;
DROP TABLE IF EXISTS contract_record;
DROP TABLE IF EXISTS doc_parse_result;
DROP TABLE IF EXISTS file_asset;
DROP TABLE IF EXISTS automation_module;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_org;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE sys_org (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父级ID',
  org_code VARCHAR(64) NOT NULL COMMENT '组织编码',
  org_name VARCHAR(128) NOT NULL COMMENT '组织名称',
  org_type VARCHAR(32) NOT NULL DEFAULT 'department' COMMENT '组织类型',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
  status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '状态',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_sys_org_code (org_code),
  KEY idx_sys_org_parent (parent_id),
  KEY idx_sys_org_status (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='组织架构表';

CREATE TABLE sys_user (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  org_id BIGINT NOT NULL COMMENT '组织ID',
  username VARCHAR(64) NOT NULL COMMENT '登录用户名',
  real_name VARCHAR(64) NOT NULL COMMENT '真实姓名',
  password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
  mobile VARCHAR(32) NULL COMMENT '手机号',
  email VARCHAR(128) NULL COMMENT '邮箱',
  avatar_url VARCHAR(512) NULL COMMENT '头像地址',
  status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '状态',
  last_login_at DATETIME NULL COMMENT '最后登录时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_sys_user_username (username),
  KEY idx_sys_user_org (org_id),
  KEY idx_sys_user_status (status, deleted),
  CONSTRAINT fk_sys_user_org FOREIGN KEY (org_id) REFERENCES sys_org (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户表';

CREATE TABLE sys_role (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
  role_name VARCHAR(128) NOT NULL COMMENT '角色名称',
  data_scope VARCHAR(32) NOT NULL DEFAULT 'self' COMMENT '数据权限范围',
  status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '状态',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_sys_role_code (role_code),
  KEY idx_sys_role_status (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统角色表';

CREATE TABLE sys_permission (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  permission_code VARCHAR(128) NOT NULL COMMENT '权限编码',
  permission_name VARCHAR(128) NOT NULL COMMENT '权限名称',
  permission_type VARCHAR(32) NOT NULL DEFAULT 'api' COMMENT '权限类型',
  resource_path VARCHAR(255) NULL COMMENT '资源路径',
  parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父级ID',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
  status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '状态',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_sys_permission_code (permission_code),
  KEY idx_sys_permission_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统权限表';

CREATE TABLE sys_user_role (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_sys_user_role (user_id, role_id),
  KEY idx_sys_user_role_role (role_id),
  CONSTRAINT fk_sys_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
  CONSTRAINT fk_sys_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';

CREATE TABLE sys_role_permission (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  permission_id BIGINT NOT NULL COMMENT '权限ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_sys_role_permission (role_id, permission_id),
  KEY idx_sys_role_permission_permission (permission_id),
  CONSTRAINT fk_sys_role_permission_role FOREIGN KEY (role_id) REFERENCES sys_role (id),
  CONSTRAINT fk_sys_role_permission_permission FOREIGN KEY (permission_id) REFERENCES sys_permission (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色权限关联表';

CREATE TABLE automation_module (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  code VARCHAR(64) NOT NULL COMMENT '模块编码',
  name VARCHAR(128) NOT NULL COMMENT '模块名称',
  description VARCHAR(512) NOT NULL COMMENT '说明',
  owner VARCHAR(64) NOT NULL COMMENT '负责部门或角色',
  status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '状态',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_automation_module_code (code),
  KEY idx_automation_module_status (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企业流程自动化能力模块表';

CREATE TABLE file_asset (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  file_name VARCHAR(255) NOT NULL COMMENT '存储文件名',
  original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
  file_ext VARCHAR(32) NULL COMMENT '文件扩展名',
  mime_type VARCHAR(128) NULL COMMENT '文件MIME类型',
  file_size BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小，单位字节',
  file_hash VARCHAR(128) NOT NULL COMMENT '文件哈希值',
  storage_bucket VARCHAR(128) NOT NULL COMMENT '对象存储桶名称',
  storage_key VARCHAR(512) NOT NULL COMMENT '对象存储键',
  business_type VARCHAR(64) NOT NULL COMMENT '业务类型',
  owner_user_id BIGINT NULL COMMENT '归属用户ID',
  org_id BIGINT NULL COMMENT '组织ID',
  parse_status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '解析状态',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_file_asset_hash (file_hash),
  KEY idx_file_asset_business (business_type, parse_status),
  KEY idx_file_asset_owner (owner_user_id),
  KEY idx_file_asset_org (org_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件资产表';

CREATE TABLE doc_parse_result (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  file_id BIGINT NOT NULL COMMENT '文件ID',
  page_no INT NOT NULL DEFAULT 1 COMMENT '页码',
  raw_text LONGTEXT NULL COMMENT '原始识别文本',
  layout_json JSON NULL COMMENT '版面解析JSON',
  ocr_engine VARCHAR(64) NULL COMMENT 'OCR引擎',
  confidence DECIMAL(5,4) NOT NULL DEFAULT 0 COMMENT '置信度',
  status VARCHAR(32) NOT NULL DEFAULT 'completed' COMMENT '状态',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_doc_parse_file (file_id, page_no),
  CONSTRAINT fk_doc_parse_file FOREIGN KEY (file_id) REFERENCES file_asset (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文档解析结果表';

CREATE TABLE contract_record (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  contract_no VARCHAR(128) NULL COMMENT '合同编号',
  file_id BIGINT NULL COMMENT '文件ID',
  party_a VARCHAR(255) NULL COMMENT '甲方名称',
  party_b VARCHAR(255) NULL COMMENT '乙方名称',
  amount DECIMAL(20,2) NULL COMMENT '合同金额',
  currency VARCHAR(16) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  start_date DATE NULL COMMENT '开始日期',
  end_date DATE NULL COMMENT '结束日期',
  sign_date DATE NULL COMMENT '签署日期',
  payment_terms TEXT NULL COMMENT '付款条款',
  invoice_terms TEXT NULL COMMENT '开票条款',
  renewal_terms TEXT NULL COMMENT '续约条款',
  risk_level VARCHAR(32) NOT NULL DEFAULT 'low' COMMENT '风险等级',
  review_status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '复核状态',
  owner_user_id BIGINT NULL COMMENT '归属用户ID',
  org_id BIGINT NULL COMMENT '组织ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_contract_no (contract_no),
  KEY idx_contract_party_a (party_a),
  KEY idx_contract_party_b (party_b),
  KEY idx_contract_end_date (end_date),
  KEY idx_contract_review (review_status, risk_level),
  CONSTRAINT fk_contract_file FOREIGN KEY (file_id) REFERENCES file_asset (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='合同台账表';

CREATE TABLE contract_risk_item (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  contract_id BIGINT NOT NULL COMMENT '合同ID',
  risk_code VARCHAR(64) NOT NULL COMMENT '风险编码',
  risk_name VARCHAR(128) NOT NULL COMMENT '风险名称',
  risk_level VARCHAR(32) NOT NULL COMMENT '风险等级',
  evidence TEXT NULL COMMENT '风险依据',
  suggestion TEXT NULL COMMENT '处理建议',
  status VARCHAR(32) NOT NULL DEFAULT 'open' COMMENT '状态',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_contract_risk_contract (contract_id),
  KEY idx_contract_risk_level (risk_level, status),
  CONSTRAINT fk_contract_risk_contract FOREIGN KEY (contract_id) REFERENCES contract_record (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='合同风险项表';

CREATE TABLE contract_reminder (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  contract_id BIGINT NOT NULL COMMENT '合同ID',
  reminder_type VARCHAR(64) NOT NULL COMMENT '提醒类型',
  reminder_time DATETIME NOT NULL COMMENT '提醒时间',
  receiver_user_id BIGINT NULL COMMENT '接收用户ID',
  status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '状态',
  sent_at DATETIME NULL COMMENT '发送时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_contract_reminder_contract (contract_id),
  KEY idx_contract_reminder_time (reminder_time, status),
  CONSTRAINT fk_contract_reminder_contract FOREIGN KEY (contract_id) REFERENCES contract_record (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='合同提醒表';

CREATE TABLE invoice_record (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  invoice_no VARCHAR(128) NOT NULL COMMENT '发票号码',
  invoice_code VARCHAR(128) NULL COMMENT '发票代码',
  file_id BIGINT NULL COMMENT '文件ID',
  buyer_name VARCHAR(255) NULL COMMENT '购买方名称',
  buyer_tax_no VARCHAR(64) NULL COMMENT '购买方税号',
  seller_name VARCHAR(255) NULL COMMENT '销售方名称',
  seller_tax_no VARCHAR(64) NULL COMMENT '销售方税号',
  invoice_date DATE NULL COMMENT '开票日期',
  amount DECIMAL(20,2) NOT NULL DEFAULT 0 COMMENT '不含税金额',
  tax_amount DECIMAL(20,2) NOT NULL DEFAULT 0 COMMENT '税额',
  total_amount DECIMAL(20,2) NOT NULL DEFAULT 0 COMMENT '价税合计金额',
  tax_rate VARCHAR(32) NULL COMMENT '税率',
  verify_status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '校验状态',
  duplicate_flag TINYINT NOT NULL DEFAULT 0 COMMENT '重复发票标识',
  owner_user_id BIGINT NULL COMMENT '归属用户ID',
  org_id BIGINT NULL COMMENT '组织ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_invoice_no_code (invoice_no, invoice_code),
  KEY idx_invoice_seller (seller_tax_no, invoice_date),
  KEY idx_invoice_status (verify_status, duplicate_flag),
  CONSTRAINT fk_invoice_file FOREIGN KEY (file_id) REFERENCES file_asset (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='发票记录表';

CREATE TABLE reconciliation_batch (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  batch_no VARCHAR(128) NOT NULL COMMENT '批次编号',
  supplier_name VARCHAR(255) NULL COMMENT '供应商名称',
  period_start DATE NOT NULL COMMENT '账期开始日期',
  period_end DATE NOT NULL COMMENT '账期结束日期',
  expected_amount DECIMAL(20,2) NOT NULL DEFAULT 0 COMMENT '应有金额',
  actual_amount DECIMAL(20,2) NOT NULL DEFAULT 0 COMMENT '实际金额',
  diff_amount DECIMAL(20,2) NOT NULL DEFAULT 0 COMMENT '差异金额',
  status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '状态',
  owner_user_id BIGINT NULL COMMENT '归属用户ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_reconciliation_batch_no (batch_no),
  KEY idx_reconciliation_batch_period (period_start, period_end),
  KEY idx_reconciliation_batch_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对账批次表';

CREATE TABLE reconciliation_item (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  batch_id BIGINT NOT NULL COMMENT '批次ID',
  invoice_id BIGINT NULL COMMENT '发票ID',
  item_type VARCHAR(64) NOT NULL COMMENT '明细类型',
  source_no VARCHAR(128) NULL COMMENT '来源单号',
  expected_amount DECIMAL(20,2) NOT NULL DEFAULT 0 COMMENT '应有金额',
  actual_amount DECIMAL(20,2) NOT NULL DEFAULT 0 COMMENT '实际金额',
  diff_amount DECIMAL(20,2) NOT NULL DEFAULT 0 COMMENT '差异金额',
  diff_reason VARCHAR(512) NULL COMMENT '差异原因',
  status VARCHAR(32) NOT NULL DEFAULT 'open' COMMENT '状态',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_reconciliation_item_batch (batch_id),
  KEY idx_reconciliation_item_status (status),
  CONSTRAINT fk_reconciliation_item_batch FOREIGN KEY (batch_id) REFERENCES reconciliation_batch (id),
  CONSTRAINT fk_reconciliation_item_invoice FOREIGN KEY (invoice_id) REFERENCES invoice_record (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对账明细表';

CREATE TABLE ticket (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  ticket_no VARCHAR(128) NOT NULL COMMENT '工单编号',
  customer_name VARCHAR(255) NULL COMMENT '客户名称',
  source_channel VARCHAR(64) NOT NULL DEFAULT 'manual' COMMENT '来源渠道',
  title VARCHAR(255) NOT NULL COMMENT '标题',
  content TEXT NULL COMMENT '工单内容',
  category VARCHAR(128) NULL COMMENT '分类',
  priority VARCHAR(32) NOT NULL DEFAULT 'P3' COMMENT '优先级',
  sentiment VARCHAR(32) NULL COMMENT '情绪倾向',
  sla_deadline DATETIME NULL COMMENT 'SLA截止时间',
  assignee_user_id BIGINT NULL COMMENT '处理人用户ID',
  status VARCHAR(32) NOT NULL DEFAULT 'open' COMMENT '状态',
  confidence DECIMAL(5,4) NOT NULL DEFAULT 0 COMMENT '置信度',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_ticket_no (ticket_no),
  KEY idx_ticket_category (category),
  KEY idx_ticket_priority_status (priority, status),
  KEY idx_ticket_sla (sla_deadline, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客服工单表';

CREATE TABLE ticket_reply_suggestion (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  ticket_id BIGINT NOT NULL COMMENT '工单ID',
  suggestion_text TEXT NOT NULL COMMENT '建议回复内容',
  citation_json JSON NULL COMMENT '引用来源JSON',
  confidence DECIMAL(5,4) NOT NULL DEFAULT 0 COMMENT '置信度',
  accepted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否采纳标识',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_ticket_reply_ticket (ticket_id),
  CONSTRAINT fk_ticket_reply_ticket FOREIGN KEY (ticket_id) REFERENCES ticket (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工单AI回复建议表';

CREATE TABLE sales_customer (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  customer_name VARCHAR(255) NOT NULL COMMENT '客户名称',
  industry VARCHAR(128) NULL COMMENT '行业',
  region VARCHAR(128) NULL COMMENT '地区',
  contact_name VARCHAR(128) NULL COMMENT '联系人姓名',
  contact_mobile VARCHAR(32) NULL COMMENT '联系人手机号',
  owner_user_id BIGINT NULL COMMENT '归属用户ID',
  customer_level VARCHAR(32) NOT NULL DEFAULT 'normal' COMMENT '客户等级',
  status VARCHAR(32) NOT NULL DEFAULT 'active' COMMENT '状态',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_sales_customer_owner (owner_user_id),
  KEY idx_sales_customer_level (customer_level, status),
  KEY idx_sales_customer_name (customer_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='销售客户表';

CREATE TABLE sales_opportunity (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  customer_id BIGINT NOT NULL COMMENT '客户ID',
  opportunity_name VARCHAR(255) NOT NULL COMMENT '商机名称',
  stage VARCHAR(64) NOT NULL DEFAULT 'lead' COMMENT '销售阶段',
  expected_amount DECIMAL(20,2) NOT NULL DEFAULT 0 COMMENT '应有金额',
  expected_close_date DATE NULL COMMENT '预计成交日期',
  win_probability DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '赢单概率',
  owner_user_id BIGINT NULL COMMENT '归属用户ID',
  status VARCHAR(32) NOT NULL DEFAULT 'open' COMMENT '状态',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_sales_opportunity_customer (customer_id),
  KEY idx_sales_opportunity_stage (stage, status),
  KEY idx_sales_opportunity_owner (owner_user_id),
  CONSTRAINT fk_sales_opportunity_customer FOREIGN KEY (customer_id) REFERENCES sales_customer (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='销售商机表';

CREATE TABLE sales_followup_task (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  opportunity_id BIGINT NULL COMMENT '商机ID',
  customer_id BIGINT NOT NULL COMMENT '客户ID',
  task_title VARCHAR(255) NOT NULL COMMENT '任务标题',
  next_action VARCHAR(512) NOT NULL COMMENT '下一步动作',
  due_time DATETIME NOT NULL COMMENT '截止时间',
  owner_user_id BIGINT NULL COMMENT '归属用户ID',
  status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '状态',
  source_type VARCHAR(64) NOT NULL DEFAULT 'ai' COMMENT '来源类型',
  source_ref_id BIGINT NULL COMMENT '来源引用ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_sales_followup_customer (customer_id),
  KEY idx_sales_followup_due (due_time, status),
  KEY idx_sales_followup_owner (owner_user_id),
  CONSTRAINT fk_sales_followup_customer FOREIGN KEY (customer_id) REFERENCES sales_customer (id),
  CONSTRAINT fk_sales_followup_opportunity FOREIGN KEY (opportunity_id) REFERENCES sales_opportunity (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='销售跟进任务表';

CREATE TABLE kb_space (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  space_code VARCHAR(64) NOT NULL COMMENT '知识库空间编码',
  space_name VARCHAR(128) NOT NULL COMMENT '知识库空间名称',
  permission_scope VARCHAR(64) NOT NULL DEFAULT 'company' COMMENT '权限范围',
  owner_org_id BIGINT NULL COMMENT '归属组织ID',
  status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '状态',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_kb_space_code (space_code),
  KEY idx_kb_space_scope (permission_scope, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识库空间表';

CREATE TABLE kb_document (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  space_id BIGINT NOT NULL COMMENT '知识库空间ID',
  file_id BIGINT NULL COMMENT '文件ID',
  title VARCHAR(255) NOT NULL COMMENT '标题',
  doc_type VARCHAR(64) NOT NULL DEFAULT 'file' COMMENT '文档类型',
  source_url VARCHAR(512) NULL COMMENT '来源URL',
  version_no VARCHAR(64) NOT NULL DEFAULT 'v1' COMMENT '版本号',
  indexing_status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '索引状态',
  status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '状态',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_kb_document_space (space_id),
  KEY idx_kb_document_status (indexing_status, status),
  CONSTRAINT fk_kb_document_space FOREIGN KEY (space_id) REFERENCES kb_space (id),
  CONSTRAINT fk_kb_document_file FOREIGN KEY (file_id) REFERENCES file_asset (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识库文档表';

CREATE TABLE kb_chunk (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  document_id BIGINT NOT NULL COMMENT '文档ID',
  chunk_index INT NOT NULL COMMENT '切片序号',
  content TEXT NOT NULL COMMENT '知识切片内容',
  token_count INT NOT NULL DEFAULT 0 COMMENT 'Token数量',
  embedding_model VARCHAR(128) NULL COMMENT '向量模型名称',
  vector_ref VARCHAR(255) NULL COMMENT '向量存储引用',
  metadata_json JSON NULL COMMENT '元数据JSON',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_kb_chunk_doc_index (document_id, chunk_index),
  KEY idx_kb_chunk_vector (vector_ref),
  CONSTRAINT fk_kb_chunk_document FOREIGN KEY (document_id) REFERENCES kb_document (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识库切片表';

CREATE TABLE report_template (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  template_code VARCHAR(64) NOT NULL COMMENT '模板编码',
  template_name VARCHAR(128) NOT NULL COMMENT '模板名称',
  report_type VARCHAR(64) NOT NULL COMMENT '报表类型',
  template_content LONGTEXT NOT NULL COMMENT '模板内容',
  audience VARCHAR(128) NULL COMMENT '目标受众',
  status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '状态',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_report_template_code (template_code),
  KEY idx_report_template_type (report_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='报表模板表';

CREATE TABLE report_record (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  template_id BIGINT NULL COMMENT '模板ID',
  report_type VARCHAR(64) NOT NULL COMMENT '报表类型',
  title VARCHAR(255) NOT NULL COMMENT '标题',
  summary TEXT NULL COMMENT '摘要',
  content LONGTEXT NULL COMMENT '报表正文',
  source_json JSON NULL COMMENT '数据来源JSON',
  generated_by BIGINT NULL COMMENT '生成人用户ID',
  send_status VARCHAR(32) NOT NULL DEFAULT 'draft' COMMENT '发送状态',
  generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_report_record_type (report_type, generated_at),
  KEY idx_report_record_status (send_status),
  CONSTRAINT fk_report_record_template FOREIGN KEY (template_id) REFERENCES report_template (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='报表生成记录表';

CREATE TABLE workflow_definition (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  workflow_code VARCHAR(64) NOT NULL COMMENT '流程编码',
  workflow_name VARCHAR(128) NOT NULL COMMENT '流程名称',
  scenario VARCHAR(64) NOT NULL COMMENT '业务场景',
  version_no INT NOT NULL DEFAULT 1 COMMENT '版本号',
  definition_json JSON NOT NULL COMMENT '流程定义JSON',
  status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '状态',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_workflow_definition_code_version (workflow_code, version_no),
  KEY idx_workflow_definition_scenario (scenario, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程定义表';

CREATE TABLE workflow_instance (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  definition_id BIGINT NOT NULL COMMENT '流程定义ID',
  business_type VARCHAR(64) NOT NULL COMMENT '业务类型',
  business_id BIGINT NULL COMMENT '业务ID',
  initiator_user_id BIGINT NULL COMMENT '发起人用户ID',
  current_node VARCHAR(128) NULL COMMENT '当前节点',
  variables_json JSON NULL COMMENT '流程变量JSON',
  status VARCHAR(32) NOT NULL DEFAULT 'running' COMMENT '状态',
  started_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  ended_at DATETIME NULL COMMENT '结束时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_workflow_instance_definition (definition_id),
  KEY idx_workflow_instance_business (business_type, business_id),
  KEY idx_workflow_instance_status (status, started_at),
  CONSTRAINT fk_workflow_instance_definition FOREIGN KEY (definition_id) REFERENCES workflow_definition (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程实例表';

CREATE TABLE workflow_task (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  instance_id BIGINT NOT NULL COMMENT '流程实例ID',
  node_code VARCHAR(128) NOT NULL COMMENT '节点编码',
  node_name VARCHAR(128) NOT NULL COMMENT '节点名称',
  assignee_user_id BIGINT NULL COMMENT '处理人用户ID',
  task_type VARCHAR(64) NOT NULL DEFAULT 'manual' COMMENT '任务类型',
  status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '状态',
  due_time DATETIME NULL COMMENT '截止时间',
  completed_at DATETIME NULL COMMENT '完成时间',
  result_json JSON NULL COMMENT '处理结果JSON',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_workflow_task_instance (instance_id),
  KEY idx_workflow_task_assignee (assignee_user_id, status),
  KEY idx_workflow_task_due (due_time, status),
  CONSTRAINT fk_workflow_task_instance FOREIGN KEY (instance_id) REFERENCES workflow_instance (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='流程任务表';

CREATE TABLE workflow_action_log (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  instance_id BIGINT NOT NULL COMMENT '流程实例ID',
  task_id BIGINT NULL COMMENT '流程任务ID',
  node_code VARCHAR(128) NOT NULL COMMENT '节点编码',
  action VARCHAR(32) NOT NULL COMMENT '审批动作',
  operator_user_id BIGINT NULL COMMENT '操作人用户ID',
  target_user_id BIGINT NULL COMMENT '目标用户ID',
  comment VARCHAR(1000) NULL COMMENT '审批意见',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '动作发生时间',
  KEY idx_workflow_action_instance (instance_id, created_at),
  KEY idx_workflow_action_task (task_id),
  CONSTRAINT fk_workflow_action_instance FOREIGN KEY (instance_id) REFERENCES workflow_instance (id),
  CONSTRAINT fk_workflow_action_task FOREIGN KEY (task_id) REFERENCES workflow_task (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审批动作历史表';

CREATE TABLE review_task (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  task_no VARCHAR(128) NOT NULL COMMENT '任务编号',
  scenario VARCHAR(64) NOT NULL COMMENT '业务场景',
  business_type VARCHAR(64) NOT NULL COMMENT '业务类型',
  business_id BIGINT NOT NULL COMMENT '业务ID',
  title VARCHAR(255) NOT NULL COMMENT '标题',
  risk_level VARCHAR(32) NOT NULL DEFAULT 'medium' COMMENT '风险等级',
  assignee_user_id BIGINT NULL COMMENT '处理人用户ID',
  status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '状态',
  review_result TEXT NULL COMMENT '复核结果',
  completed_at DATETIME NULL COMMENT '完成时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_review_task_no (task_no),
  KEY idx_review_task_business (business_type, business_id),
  KEY idx_review_task_assignee (assignee_user_id, status),
  KEY idx_review_task_risk (risk_level, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='人工复核任务表';

CREATE TABLE message_notification (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  channel VARCHAR(64) NOT NULL COMMENT '消息渠道',
  receiver_user_id BIGINT NULL COMMENT '接收用户ID',
  receiver_address VARCHAR(255) NULL COMMENT '接收地址',
  title VARCHAR(255) NOT NULL COMMENT '标题',
  content TEXT NOT NULL COMMENT '消息内容',
  business_type VARCHAR(64) NULL COMMENT '业务类型',
  business_id BIGINT NULL COMMENT '业务ID',
  send_status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '发送状态',
  retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
  sent_at DATETIME NULL COMMENT '发送时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_message_receiver (receiver_user_id, send_status),
  KEY idx_message_business (business_type, business_id),
  KEY idx_message_status (send_status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息通知表';

CREATE TABLE integration_connector (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  connector_code VARCHAR(64) NOT NULL COMMENT '连接器编码',
  connector_name VARCHAR(128) NOT NULL COMMENT '连接器名称',
  connector_type VARCHAR(64) NOT NULL COMMENT '连接器类型',
  auth_config_json JSON NULL COMMENT '认证配置JSON',
  endpoint_url VARCHAR(512) NULL COMMENT '接口地址',
  status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '状态',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_integration_connector_code (connector_code),
  KEY idx_integration_connector_type (connector_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='第三方集成连接器表';

CREATE TABLE webhook_event (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  connector_id BIGINT NULL COMMENT '连接器ID',
  event_type VARCHAR(128) NOT NULL COMMENT '事件类型',
  event_key VARCHAR(128) NULL COMMENT '事件键',
  payload_json JSON NOT NULL COMMENT '事件载荷JSON',
  process_status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '处理状态',
  error_message VARCHAR(1024) NULL COMMENT '错误信息',
  received_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '接收时间',
  processed_at DATETIME NULL COMMENT '处理时间',
  KEY idx_webhook_event_connector (connector_id),
  KEY idx_webhook_event_status (process_status, received_at),
  KEY idx_webhook_event_key (event_key),
  CONSTRAINT fk_webhook_event_connector FOREIGN KEY (connector_id) REFERENCES integration_connector (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Webhook事件表';

CREATE TABLE ai_model_provider (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  provider_code VARCHAR(64) NOT NULL COMMENT '模型供应商编码',
  provider_name VARCHAR(128) NOT NULL COMMENT '模型供应商名称',
  provider_type VARCHAR(64) NOT NULL DEFAULT 'llm' COMMENT '模型供应商类型',
  base_url VARCHAR(512) NULL COMMENT '基础调用地址',
  default_model VARCHAR(128) NULL COMMENT '默认模型',
  status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '供应商状态',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_ai_model_provider_code (provider_code),
  KEY idx_ai_model_provider_type (provider_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI模型供应商表';

CREATE TABLE ai_prompt_template (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  template_code VARCHAR(128) NOT NULL COMMENT '模板编码',
  scenario VARCHAR(64) NOT NULL COMMENT '业务场景',
  version_no INT NOT NULL DEFAULT 1 COMMENT '版本号',
  model_name VARCHAR(128) NULL COMMENT '模型名称',
  system_prompt TEXT NULL COMMENT '系统提示词',
  user_prompt_template TEXT NOT NULL COMMENT '用户提示词模板',
  output_schema_json JSON NULL COMMENT '输出结构JSON',
  status VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '模板状态',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_ai_prompt_template_code_version (template_code, version_no),
  KEY idx_ai_prompt_template_scenario (scenario, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI提示词模板表';

CREATE TABLE ai_model_call_log (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  provider_id BIGINT NULL COMMENT '模型供应商ID',
  scenario VARCHAR(64) NOT NULL COMMENT '业务场景',
  business_type VARCHAR(64) NULL COMMENT '业务类型',
  business_id BIGINT NULL COMMENT '业务ID',
  model_name VARCHAR(128) NULL COMMENT '模型名称',
  prompt_template_code VARCHAR(128) NULL COMMENT '提示词模板编码',
  request_tokens INT NOT NULL DEFAULT 0 COMMENT '请求Token数',
  response_tokens INT NOT NULL DEFAULT 0 COMMENT '响应Token数',
  cost_amount DECIMAL(18,6) NOT NULL DEFAULT 0 COMMENT '调用成本金额',
  latency_ms INT NOT NULL DEFAULT 0 COMMENT '调用耗时毫秒',
  success_flag TINYINT NOT NULL DEFAULT 1 COMMENT '调用成功标识',
  error_message VARCHAR(1024) NULL COMMENT '错误信息',
  request_hash VARCHAR(128) NULL COMMENT '请求哈希',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_ai_model_call_scenario (scenario, created_at),
  KEY idx_ai_model_call_business (business_type, business_id),
  KEY idx_ai_model_call_provider (provider_id),
  CONSTRAINT fk_ai_model_call_provider FOREIGN KEY (provider_id) REFERENCES ai_model_provider (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI模型调用日志表';

CREATE TABLE ai_evaluation_sample (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  scenario VARCHAR(64) NOT NULL COMMENT '业务场景',
  input_text LONGTEXT NOT NULL COMMENT '输入文本',
  expected_output JSON NULL COMMENT '期望输出JSON',
  actual_output JSON NULL COMMENT '实际输出JSON',
  score DECIMAL(5,2) NULL COMMENT '评测分数',
  reviewer_user_id BIGINT NULL COMMENT '评测人用户ID',
  review_comment VARCHAR(1024) NULL COMMENT '评测备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_ai_evaluation_scenario (scenario, created_at),
  KEY idx_ai_evaluation_score (score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI效果评测样本表';

CREATE TABLE audit_log (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  operator_user_id BIGINT NULL COMMENT '操作人用户ID',
  operator_name VARCHAR(128) NULL COMMENT '操作人名称',
  action_code VARCHAR(128) NOT NULL COMMENT '操作编码',
  action_name VARCHAR(128) NOT NULL COMMENT '操作名称',
  target_type VARCHAR(64) NULL COMMENT '目标类型',
  target_id BIGINT NULL COMMENT '目标ID',
  request_ip VARCHAR(64) NULL COMMENT '请求IP',
  user_agent VARCHAR(512) NULL COMMENT '用户代理',
  before_json JSON NULL COMMENT '变更前JSON',
  after_json JSON NULL COMMENT '变更后JSON',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_audit_operator (operator_user_id, created_at),
  KEY idx_audit_target (target_type, target_id),
  KEY idx_audit_action (action_code, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审计日志表';

CREATE TABLE field_correction_history (
  id BIGINT NOT NULL PRIMARY KEY COMMENT '主键ID',
  batch_id BIGINT NOT NULL COMMENT '同一次保存操作的修正批次ID',
  file_id BIGINT NOT NULL COMMENT '文件资产ID',
  business_type VARCHAR(64) NULL COMMENT '关联业务类型',
  business_id BIGINT NULL COMMENT '关联业务记录ID',
  field_key VARCHAR(128) NOT NULL COMMENT '字段编码',
  field_name VARCHAR(128) NOT NULL COMMENT '字段中文名称',
  old_value TEXT NULL COMMENT '修正前字段值',
  new_value TEXT NULL COMMENT '修正后字段值',
  reason VARCHAR(1000) NOT NULL COMMENT '修正原因',
  operator_user_id BIGINT NULL COMMENT '操作人用户ID',
  operator_name VARCHAR(128) NULL COMMENT '操作人名称',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修正时间',
  KEY idx_field_correction_file (file_id, created_at),
  KEY idx_field_correction_batch (batch_id),
  KEY idx_field_correction_business (business_type, business_id),
  KEY idx_field_correction_operator (operator_user_id, created_at),
  CONSTRAINT fk_field_correction_file FOREIGN KEY (file_id) REFERENCES file_asset (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件抽取字段修正历史表';

INSERT INTO automation_module (id, code, name, description, owner, status, sort_order)
VALUES
  (1001, 'contract', 'Contract Extraction', 'Extract parties, amount, terms, dates and risks from contracts.', 'Legal/Admin', 'enabled', 10),
  (1002, 'invoice', 'Invoice Reconciliation', 'Parse invoices, detect duplicates and reconcile supplier statements.', 'Finance', 'enabled', 20),
  (1003, 'ticket', 'Ticket Classification', 'Classify ticket category, priority, sentiment and suggested owner.', 'Customer Service', 'enabled', 30),
  (1004, 'sales', 'Sales Follow-up', 'Generate next actions and overdue reminders from customer activities.', 'Sales', 'enabled', 40),
  (1005, 'kb', 'Knowledge Base QA', 'Answer questions from enterprise documents with citations.', 'Operations', 'enabled', 50),
  (1006, 'report', 'Daily and Weekly Reports', 'Generate progress, risk, todo and operation summaries.', 'Management', 'enabled', 60)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  owner = VALUES(owner),
  status = VALUES(status),
  sort_order = VALUES(sort_order),
  updated_at = CURRENT_TIMESTAMP;
