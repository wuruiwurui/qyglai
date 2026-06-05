-- Navicat import verification SQL
-- Run this after importing qyglai_schema.sql or qyglai_schema_navicat_current_db.sql

SELECT DATABASE() AS current_database;

SELECT COUNT(*) AS table_count
FROM information_schema.tables
WHERE table_schema = DATABASE();

SELECT table_name, table_comment
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
    'sys_user', 'automation_module', 'contract_record', 'invoice_record',
    'ticket', 'sales_customer', 'kb_document', 'workflow_instance',
    'ai_model_call_log', 'audit_log'
  )
ORDER BY table_name;

SELECT COUNT(*) AS automation_module_count FROM automation_module;
SELECT * FROM automation_module ORDER BY sort_order;
