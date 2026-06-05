-- 修复文件资产表哈希索引。
-- 同一文件可能被不同业务重复上传或重新解析，因此 file_hash 不应作为全局唯一约束。

SET @index_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'file_asset'
    AND index_name = 'uk_file_asset_hash'
);

SET @drop_sql := IF(
  @index_exists > 0,
  'ALTER TABLE file_asset DROP INDEX uk_file_asset_hash',
  'SELECT 1'
);

PREPARE drop_stmt FROM @drop_sql;
EXECUTE drop_stmt;
DEALLOCATE PREPARE drop_stmt;

SET @normal_index_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'file_asset'
    AND index_name = 'idx_file_asset_hash'
);

SET @create_sql := IF(
  @normal_index_exists = 0,
  'CREATE INDEX idx_file_asset_hash ON file_asset (file_hash)',
  'SELECT 1'
);

PREPARE create_stmt FROM @create_sql;
EXECUTE create_stmt;
DEALLOCATE PREPARE create_stmt;
