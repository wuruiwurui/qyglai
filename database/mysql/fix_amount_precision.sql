-- 修复金额字段精度，避免合同、发票、对账、销售商机的大额数据入库溢出。
-- 适用数据库：MySQL 8.x
-- 执行方式：在 Navicat 中选择 qyglai 数据库后运行本文件，或使用 mysql -D qyglai 执行。

ALTER TABLE contract_record
  MODIFY amount DECIMAL(20,2) NULL COMMENT '合同金额';

ALTER TABLE invoice_record
  MODIFY amount DECIMAL(20,2) NOT NULL DEFAULT 0 COMMENT '不含税金额',
  MODIFY tax_amount DECIMAL(20,2) NOT NULL DEFAULT 0 COMMENT '税额',
  MODIFY total_amount DECIMAL(20,2) NOT NULL DEFAULT 0 COMMENT '价税合计金额';

ALTER TABLE reconciliation_batch
  MODIFY expected_amount DECIMAL(20,2) NOT NULL DEFAULT 0 COMMENT '应有金额',
  MODIFY actual_amount DECIMAL(20,2) NOT NULL DEFAULT 0 COMMENT '实际金额',
  MODIFY diff_amount DECIMAL(20,2) NOT NULL DEFAULT 0 COMMENT '差异金额';

ALTER TABLE reconciliation_item
  MODIFY expected_amount DECIMAL(20,2) NOT NULL DEFAULT 0 COMMENT '应有金额',
  MODIFY actual_amount DECIMAL(20,2) NOT NULL DEFAULT 0 COMMENT '实际金额',
  MODIFY diff_amount DECIMAL(20,2) NOT NULL DEFAULT 0 COMMENT '差异金额';

ALTER TABLE sales_opportunity
  MODIFY expected_amount DECIMAL(20,2) NOT NULL DEFAULT 0 COMMENT '应有金额';
