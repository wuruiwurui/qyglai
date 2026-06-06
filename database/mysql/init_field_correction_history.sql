USE qyglai;

CREATE TABLE IF NOT EXISTS field_correction_history (
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
