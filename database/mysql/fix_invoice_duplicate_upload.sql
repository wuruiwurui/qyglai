-- 允许同一张发票多次上传入库。
-- 原唯一索引 uk_invoice_no_code 会阻止相同 invoice_no + invoice_code 的发票再次插入。
-- 改为普通索引后，每次上传都能生成独立发票记录，并通过 duplicate_flag 标记是否重复。

ALTER TABLE invoice_record
  DROP INDEX uk_invoice_no_code;

ALTER TABLE invoice_record
  ADD INDEX idx_invoice_no_code (invoice_no, invoice_code);
