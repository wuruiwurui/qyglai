-- 完整权限管理初始化数据。
-- 为默认管理员创建超级管理员角色和通配权限，保证启用方法级权限校验后仍可管理系统。

INSERT INTO sys_role (id, role_code, role_name, data_scope, status, deleted, created_at, updated_at)
VALUES (900000000000000001, 'SUPER_ADMIN', 'Super Administrator', 'all', 'enabled', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name), data_scope = 'all', status = 'enabled';

INSERT INTO sys_permission (id, permission_code, permission_name, permission_type, resource_path, parent_id, sort_order, status, deleted, created_at, updated_at)
VALUES
  (900000000000000101, '*', 'All Permissions', 'api', '*', 0, 1, 'enabled', 0, NOW(), NOW()),
  (900000000000000102, 'system:manage', 'System Permission Management', 'menu', '/api/system/**', 0, 2, 'enabled', 0, NOW(), NOW()),
  (900000000000000103, 'file:view', 'File Management', 'menu', '/api/files/**', 0, 3, 'enabled', 0, NOW(), NOW()),
  (900000000000000104, 'contract:view', 'Contract Management', 'menu', '/api/contracts/**', 0, 4, 'enabled', 0, NOW(), NOW()),
  (900000000000000105, 'finance:view', 'Finance Management', 'menu', '/api/invoices/**', 0, 5, 'enabled', 0, NOW(), NOW()),
  (900000000000000106, 'ticket:view', 'Ticket Management', 'menu', '/api/tickets/**', 0, 6, 'enabled', 0, NOW(), NOW()),
  (900000000000000107, 'sales:view', 'Sales Management', 'menu', '/api/sales/**', 0, 7, 'enabled', 0, NOW(), NOW()),
  (900000000000000108, 'kb:view', 'Knowledge Base', 'menu', '/api/kb/**', 0, 8, 'enabled', 0, NOW(), NOW()),
  (900000000000000109, 'report:view', 'Report Management', 'menu', '/api/reports/**', 0, 9, 'enabled', 0, NOW(), NOW()),
  (900000000000000110, 'workflow:view', 'Workflow Management', 'menu', '/api/workflows/**', 0, 10, 'enabled', 0, NOW(), NOW()),
  (900000000000000111, 'review:view', 'Review Management', 'menu', '/api/review/**', 0, 11, 'enabled', 0, NOW(), NOW()),
  (900000000000000112, 'notification:view', 'Notification Management', 'menu', '/api/notifications/**', 0, 12, 'enabled', 0, NOW(), NOW()),
  (900000000000000113, 'integration:view', 'Integration Management', 'menu', '/api/integrations/**', 0, 13, 'enabled', 0, NOW(), NOW()),
  (900000000000000114, 'ai:manage', 'AI Governance', 'menu', '/api/ai-governance/**', 0, 14, 'enabled', 0, NOW(), NOW()),
  (900000000000000115, 'audit:view', 'Audit Management', 'menu', '/api/audit/**', 0, 15, 'enabled', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE permission_name = VALUES(permission_name), status = 'enabled';

INSERT INTO sys_role_permission (id, role_id, permission_id, created_at)
VALUES
  (900000000000000201, 900000000000000001, 900000000000000101, NOW()),
  (900000000000000202, 900000000000000001, 900000000000000102, NOW())
ON DUPLICATE KEY UPDATE created_at = VALUES(created_at);

INSERT INTO sys_user_role (id, user_id, role_id, created_at)
SELECT 900000000000000301, id, 900000000000000001, NOW()
FROM sys_user
WHERE username = 'admin'
ON DUPLICATE KEY UPDATE created_at = VALUES(created_at);
