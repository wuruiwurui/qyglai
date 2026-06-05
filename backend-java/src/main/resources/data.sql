INSERT INTO automation_module (id, code, name, description, owner, status, sort_order, deleted)
SELECT 1001, 'contract', '合同信息提取', '抽取金额、主体、期限、付款节点和风险条款', '法务/行政', 'enabled', 10, 0
WHERE NOT EXISTS (SELECT 1 FROM automation_module WHERE code = 'contract');

INSERT INTO automation_module (id, code, name, description, owner, status, sort_order, deleted)
SELECT 1002, 'invoice', '发票与对账处理', '识别发票、查重、匹配对账单并标记差异', '财务', 'enabled', 20, 0
WHERE NOT EXISTS (SELECT 1 FROM automation_module WHERE code = 'invoice');

INSERT INTO automation_module (id, code, name, description, owner, status, sort_order, deleted)
SELECT 1003, 'ticket', '客服工单归类', '识别类型、优先级、情绪和建议回复', '客服', 'enabled', 30, 0
WHERE NOT EXISTS (SELECT 1 FROM automation_module WHERE code = 'ticket');

INSERT INTO automation_module (id, code, name, description, owner, status, sort_order, deleted)
SELECT 1004, 'sales', '销售跟进提醒', '从客户动态中生成下一步动作和逾期提醒', '销售', 'enabled', 40, 0
WHERE NOT EXISTS (SELECT 1 FROM automation_module WHERE code = 'sales');

INSERT INTO automation_module (id, code, name, description, owner, status, sort_order, deleted)
SELECT 1005, 'kb', '知识库问答', '基于企业资料回答问题并返回引用来源', '运营', 'enabled', 50, 0
WHERE NOT EXISTS (SELECT 1 FROM automation_module WHERE code = 'kb');

INSERT INTO automation_module (id, code, name, description, owner, status, sort_order, deleted)
SELECT 1006, 'report', '日报周报生成', '自动汇总进展、风险、待办和经营指标', '管理层', 'enabled', 60, 0
WHERE NOT EXISTS (SELECT 1 FROM automation_module WHERE code = 'report');
