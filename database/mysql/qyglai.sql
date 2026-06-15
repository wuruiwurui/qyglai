/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80032
 Source Host           : localhost:3306
 Source Schema         : qyglai

 Target Server Type    : MySQL
 Target Server Version : 80032
 File Encoding         : 65001

 Date: 05/06/2026 11:44:22
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_embedding_config
-- ----------------------------
DROP TABLE IF EXISTS `ai_embedding_config`;
CREATE TABLE `ai_embedding_config` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `enabled` tinyint NOT NULL DEFAULT 1 COMMENT '是否启用真实Embedding',
  `provider` varchar(64) NOT NULL COMMENT '模型供应商',
  `api_url` varchar(512) NOT NULL COMMENT 'Embedding完整调用地址',
  `api_key` varchar(1024) NULL COMMENT 'Embedding API密钥',
  `model` varchar(128) NOT NULL COMMENT 'Embedding模型或接入点ID',
  `dimension` int NOT NULL COMMENT '向量维度',
  `milvus_url` varchar(512) NOT NULL COMMENT 'Milvus REST地址',
  `collection_name` varchar(128) NOT NULL COMMENT 'Milvus集合名称',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Embedding与向量数据库配置表';

-- ----------------------------
-- Table structure for ai_evaluation_sample
-- ----------------------------
DROP TABLE IF EXISTS `ai_evaluation_sample`;
CREATE TABLE `ai_evaluation_sample`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `scenario` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务场景',
  `input_text` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '输入文本',
  `expected_output` json NULL COMMENT '期望输出JSON',
  `actual_output` json NULL COMMENT '实际输出JSON',
  `score` decimal(5, 2) NULL DEFAULT NULL COMMENT '评测分数',
  `reviewer_user_id` bigint(0) NULL DEFAULT NULL COMMENT '评测人用户ID',
  `review_comment` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评测备注',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_evaluation_scenario`(`scenario`, `created_at`) USING BTREE,
  INDEX `idx_ai_evaluation_score`(`score`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI效果评测样本表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_evaluation_sample
-- ----------------------------

-- ----------------------------
-- Table structure for ai_model_call_log
-- ----------------------------
DROP TABLE IF EXISTS `ai_model_call_log`;
CREATE TABLE `ai_model_call_log`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `provider_id` bigint(0) NULL DEFAULT NULL COMMENT '模型供应商ID',
  `scenario` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务场景',
  `business_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务类型',
  `business_id` bigint(0) NULL DEFAULT NULL COMMENT '业务ID',
  `model_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型名称',
  `prompt_template_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '提示词模板编码',
  `request_tokens` int(0) NOT NULL DEFAULT 0 COMMENT '请求Token数',
  `response_tokens` int(0) NOT NULL DEFAULT 0 COMMENT '响应Token数',
  `cost_amount` decimal(18, 6) NOT NULL COMMENT '调用成本金额',
  `latency_ms` int(0) NOT NULL DEFAULT 0 COMMENT '调用耗时毫秒',
  `success_flag` tinyint(0) NOT NULL DEFAULT 1 COMMENT '调用成功标识',
  `error_message` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `request_hash` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求哈希',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_model_call_scenario`(`scenario`, `created_at`) USING BTREE,
  INDEX `idx_ai_model_call_business`(`business_type`, `business_id`) USING BTREE,
  INDEX `idx_ai_model_call_provider`(`provider_id`) USING BTREE,
  CONSTRAINT `fk_ai_model_call_provider` FOREIGN KEY (`provider_id`) REFERENCES `ai_model_provider` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI模型调用日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_model_call_log
-- ----------------------------
INSERT INTO `ai_model_call_log` VALUES (2062711586580119553, NULL, 'boss_query', 'report', NULL, 'python-ai-service', 'boss/query', 32, 0, 0.000000, 48, 1, NULL, '7497885c1f853447cba50a4c308e8804', '2026-06-05 09:42:20');
INSERT INTO `ai_model_call_log` VALUES (2062711587846799362, NULL, 'file_parse_extract', 'contract', NULL, 'python-ai-service', 'files/parse-and-extract', 11, 0, 0.000000, 65, 1, NULL, 'ba627c4067f6430d52a0e840b5d21ac1', '2026-06-05 09:42:21');
INSERT INTO `ai_model_call_log` VALUES (2062716881566035969, NULL, 'boss_query', 'report', NULL, 'python-ai-service', 'boss/query', 36, 0, 0.000000, 46, 1, NULL, '479677746db610009968c14a1238644a', '2026-06-05 10:03:23');
INSERT INTO `ai_model_call_log` VALUES (2062716989858770945, NULL, 'boss_query', 'report', NULL, 'python-ai-service', 'boss/query', 31, 0, 0.000000, 5, 1, NULL, 'ec5ea45fcb58ad08df38c88980cf39c7', '2026-06-05 10:03:48');
INSERT INTO `ai_model_call_log` VALUES (2062719667812696065, NULL, 'boss_query', 'sales', NULL, 'python-ai-service', 'boss/query', 39, 0, 0.000000, 37, 1, NULL, '4f05ce16b904f8939a95f4527fcf8efc', '2026-06-05 10:14:27');
INSERT INTO `ai_model_call_log` VALUES (2062719668064354306, NULL, 'boss_query', 'finance', NULL, 'python-ai-service', 'boss/query', 37, 0, 0.000000, 18, 1, NULL, 'b47059149e6f49a713913a702f71a210', '2026-06-05 10:14:27');
INSERT INTO `ai_model_call_log` VALUES (2062719668139851778, NULL, 'boss_query', 'customer_service', NULL, 'python-ai-service', 'boss/query', 38, 0, 0.000000, 12, 1, NULL, '4d890cd32c5542e1614085722417eefe', '2026-06-05 10:14:27');
INSERT INTO `ai_model_call_log` VALUES (2062719668206960641, NULL, 'boss_query', 'report', NULL, 'python-ai-service', 'boss/query', 36, 0, 0.000000, 10, 1, NULL, '0a2ab155ee9618d4036c1a584670ed61', '2026-06-05 10:14:27');
INSERT INTO `ai_model_call_log` VALUES (2062720373521121282, NULL, 'boss_query', 'sales', NULL, 'python-ai-service', 'boss/query', 37, 0, 0.000000, 7, 1, NULL, 'dee4fb5ad8b00225a02dc9eb99b19698', '2026-06-05 10:17:15');
INSERT INTO `ai_model_call_log` VALUES (2062720690904104962, NULL, 'boss_query', 'review', NULL, 'python-ai-service', 'boss/query', 35, 0, 0.000000, 4, 1, NULL, '7684359097b4796986bd1f0deba9d4fa', '2026-06-05 10:18:31');
INSERT INTO `ai_model_call_log` VALUES (2062720702471995393, NULL, 'boss_query', 'finance', NULL, 'python-ai-service', 'boss/query', 35, 0, 0.000000, 15, 1, NULL, 'dbee63e75bdcd0c9fa9609039eba7c81', '2026-06-05 10:18:34');
INSERT INTO `ai_model_call_log` VALUES (2062720737632845825, NULL, 'boss_query', 'overview', NULL, 'python-ai-service', 'boss/query', 31, 0, 0.000000, 3, 1, NULL, 'ec5ea45fcb58ad08df38c88980cf39c7', '2026-06-05 10:18:42');
INSERT INTO `ai_model_call_log` VALUES (2062722820096393217, NULL, 'boss_query', 'overview', NULL, 'fallback-local-rule', 'boss/query', 31, 0, 0.000000, 5025, 0, '已降级到本地经营摘要：Error while extracting response for type [com.qyglai.automation.dto.BossChatResponse] and content type [application/octet-stream]', 'ec5ea45fcb58ad08df38c88980cf39c7', '2026-06-05 10:26:59');
INSERT INTO `ai_model_call_log` VALUES (2062723154944458754, NULL, 'boss_query', 'overview', NULL, 'fallback-local-rule', 'boss/query', 31, 0, 0.000000, 5004, 0, '已降级到本地经营摘要：Error while extracting response for type [com.qyglai.automation.dto.BossChatResponse] and content type [application/octet-stream]', 'ec5ea45fcb58ad08df38c88980cf39c7', '2026-06-05 10:28:18');
INSERT INTO `ai_model_call_log` VALUES (2062723316987199489, NULL, 'boss_query', 'sales', NULL, 'fallback-local-rule', 'boss/query', 38, 0, 0.000000, 5010, 0, '已降级到本地经营摘要：Error while extracting response for type [com.qyglai.automation.dto.BossChatResponse] and content type [application/octet-stream]', '4d04c3a24141655902a8686bff2eadfe', '2026-06-05 10:28:57');
INSERT INTO `ai_model_call_log` VALUES (2062723846941814786, NULL, 'boss_query', 'sales', NULL, 'python-ai-service', 'boss/query', 38, 0, 0.000000, 16007, 1, NULL, '4d04c3a24141655902a8686bff2eadfe', '2026-06-05 10:31:03');
INSERT INTO `ai_model_call_log` VALUES (2062723991318147074, NULL, 'boss_query', 'overview', NULL, 'python-ai-service', 'boss/query', 31, 0, 0.000000, 14076, 1, NULL, 'ec5ea45fcb58ad08df38c88980cf39c7', '2026-06-05 10:31:38');
INSERT INTO `ai_model_call_log` VALUES (2062724127406534657, NULL, 'boss_query', 'sales', NULL, 'python-ai-service', 'boss/query', 37, 0, 0.000000, 16325, 1, NULL, 'dee4fb5ad8b00225a02dc9eb99b19698', '2026-06-05 10:32:10');
INSERT INTO `ai_model_call_log` VALUES (2062724322764632065, NULL, 'boss_query', 'overview', NULL, 'python-ai-service', 'boss/query', 33, 0, 0.000000, 16260, 1, NULL, 'ccd2518080e3c5eba9d36ed6f79bcb17', '2026-06-05 10:32:57');
INSERT INTO `ai_model_call_log` VALUES (2062724636779589634, NULL, 'boss_query', 'contract', NULL, 'python-ai-service', 'boss/query', 33, 0, 0.000000, 14253, 1, NULL, '618023ede7f2334b6764f172b09bfec1', '2026-06-05 10:34:12');
INSERT INTO `ai_model_call_log` VALUES (2062725745891868674, NULL, 'file_parse_extract', 'invoice', NULL, 'python-ai-service', 'files/parse-and-extract', 17, 0, 0.000000, 198, 1, NULL, '7dea2de5b6769f2c9e668b538cfd878a', '2026-06-05 10:38:36');
INSERT INTO `ai_model_call_log` VALUES (2062726965603954690, NULL, 'file_parse_extract', 'invoice', NULL, 'python-ai-service', 'files/parse-and-extract', 17, 0, 0.000000, 111, 1, NULL, '7dea2de5b6769f2c9e668b538cfd878a', '2026-06-05 10:43:27');
INSERT INTO `ai_model_call_log` VALUES (2062726966396678145, NULL, 'file_parse_extract', 'invoice', NULL, 'python-ai-service', 'files/parse-and-extract', 17, 0, 0.000000, 38, 1, NULL, '7dea2de5b6769f2c9e668b538cfd878a', '2026-06-05 10:43:27');
INSERT INTO `ai_model_call_log` VALUES (2062727171934351362, NULL, 'boss_query', 'review', NULL, 'python-ai-service', 'boss/query', 35, 0, 0.000000, 16290, 1, NULL, '7684359097b4796986bd1f0deba9d4fa', '2026-06-05 10:44:16');
INSERT INTO `ai_model_call_log` VALUES (2062727191697911810, NULL, 'file_parse_extract', 'invoice', NULL, 'python-ai-service', 'files/parse-and-extract', 17, 0, 0.000000, 34, 1, NULL, '7dea2de5b6769f2c9e668b538cfd878a', '2026-06-05 10:44:21');
INSERT INTO `ai_model_call_log` VALUES (2062727546305343489, NULL, 'file_parse_extract', 'invoice', NULL, 'python-ai-service', 'files/parse-and-extract', 17, 0, 0.000000, 18, 1, NULL, '7dea2de5b6769f2c9e668b538cfd878a', '2026-06-05 10:45:45');
INSERT INTO `ai_model_call_log` VALUES (2062728690528223233, NULL, 'file_parse_extract', 'invoice', NULL, 'python-ai-service', 'files/parse-and-extract', 17, 0, 0.000000, 138, 1, NULL, '7dea2de5b6769f2c9e668b538cfd878a', '2026-06-05 10:50:18');
INSERT INTO `ai_model_call_log` VALUES (2062728819922501634, NULL, 'file_parse_extract', 'invoice', NULL, 'python-ai-service', 'files/parse-and-extract', 17, 0, 0.000000, 10, 1, NULL, '7dea2de5b6769f2c9e668b538cfd878a', '2026-06-05 10:50:49');
INSERT INTO `ai_model_call_log` VALUES (2062729349314969601, NULL, 'file_parse_extract', 'invoice', NULL, 'python-ai-service', 'files/parse-and-extract', 8, 0, 0.000000, 35, 1, NULL, '49b88a397b18784090e24449ed476508', '2026-06-05 10:52:55');
INSERT INTO `ai_model_call_log` VALUES (2062731258264354818, NULL, 'boss_query', 'report', NULL, 'python-ai-service', 'boss/query', 34, 0, 0.000000, 16504, 1, NULL, 'bbcc24ece364065c9b9de5f9927c43ea', '2026-06-05 11:00:30');
INSERT INTO `ai_model_call_log` VALUES (2062731413151612930, NULL, 'boss_query', 'review', NULL, 'python-ai-service', 'boss/query', 35, 0, 0.000000, 16146, 1, NULL, '7684359097b4796986bd1f0deba9d4fa', '2026-06-05 11:01:07');
INSERT INTO `ai_model_call_log` VALUES (2062732090942734338, NULL, 'boss_query', 'finance', NULL, 'python-ai-service', 'boss/query', 35, 0, 0.000000, 16201, 1, NULL, 'dbee63e75bdcd0c9fa9609039eba7c81', '2026-06-05 11:03:49');
INSERT INTO `ai_model_call_log` VALUES (2062732229681922049, NULL, 'boss_query', 'overview', NULL, 'python-ai-service', 'boss/query', 31, 0, 0.000000, 16180, 1, NULL, 'ec5ea45fcb58ad08df38c88980cf39c7', '2026-06-05 11:04:22');
INSERT INTO `ai_model_call_log` VALUES (2062732333478363138, NULL, 'file_parse_extract', 'invoice', NULL, 'python-ai-service', 'files/parse-and-extract', 17, 0, 0.000000, 79, 1, NULL, '7dea2de5b6769f2c9e668b538cfd878a', '2026-06-05 11:04:47');
INSERT INTO `ai_model_call_log` VALUES (2062732465716379650, NULL, 'file_parse_extract', 'invoice', NULL, 'python-ai-service', 'files/parse-and-extract', 8, 0, 0.000000, 24, 1, NULL, '49b88a397b18784090e24449ed476508', '2026-06-05 11:05:18');

-- ----------------------------
-- Table structure for ai_model_provider
-- ----------------------------
DROP TABLE IF EXISTS `ai_model_provider`;
CREATE TABLE `ai_model_provider`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `provider_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模型供应商编码',
  `provider_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模型供应商名称',
  `provider_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'llm' COMMENT '模型供应商类型',
  `base_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '基础调用地址',
  `default_model` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认模型',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'enabled' COMMENT '供应商状态',
  `deleted` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ai_model_provider_code`(`provider_code`) USING BTREE,
  INDEX `idx_ai_model_provider_type`(`provider_type`, `status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI模型供应商表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_model_provider
-- ----------------------------

-- ----------------------------
-- Table structure for ai_prompt_template
-- ----------------------------
DROP TABLE IF EXISTS `ai_prompt_template`;
CREATE TABLE `ai_prompt_template`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `template_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板编码',
  `scenario` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务场景',
  `version_no` int(0) NOT NULL DEFAULT 1 COMMENT '版本号',
  `model_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型名称',
  `system_prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '系统提示词',
  `user_prompt_template` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户提示词模板',
  `output_schema_json` json NULL COMMENT '输出结构JSON',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'enabled' COMMENT '模板状态',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ai_prompt_template_code_version`(`template_code`, `version_no`) USING BTREE,
  INDEX `idx_ai_prompt_template_scenario`(`scenario`, `status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI提示词模板表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_prompt_template
-- ----------------------------
INSERT INTO `ai_prompt_template` VALUES (2062427545930870785, 'ORG-DEMO', 'department', 1, NULL, NULL, 'demo', NULL, 'enabled', '2026-06-04 14:53:39', '2026-06-04 14:53:39');
INSERT INTO `ai_prompt_template` VALUES (2062437033262039041, 'PROMPT-UI', 'report', 1, NULL, NULL, '??????????{input}', NULL, 'enabled', '2026-06-04 15:31:21', '2026-06-04 15:31:21');

-- ----------------------------
-- Table structure for audit_log
-- ----------------------------
DROP TABLE IF EXISTS `audit_log`;
CREATE TABLE `audit_log`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `operator_user_id` bigint(0) NULL DEFAULT NULL COMMENT '操作人用户ID',
  `operator_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作人名称',
  `action_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作编码',
  `action_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作名称',
  `target_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标类型',
  `target_id` bigint(0) NULL DEFAULT NULL COMMENT '目标ID',
  `request_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求IP',
  `user_agent` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户代理',
  `before_json` json NULL COMMENT '变更前JSON',
  `after_json` json NULL COMMENT '变更后JSON',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_audit_operator`(`operator_user_id`, `created_at`) USING BTREE,
  INDEX `idx_audit_target`(`target_type`, `target_id`) USING BTREE,
  INDEX `idx_audit_action`(`action_code`, `created_at`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审计日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of audit_log
-- ----------------------------
INSERT INTO `audit_log` VALUES (2062409003575558146, NULL, 'system', 'INVOICE_PARSE', '发票解析', 'invoice_record', 2062409003508449282, NULL, NULL, NULL, NULL, '2026-06-04 13:39:58');
INSERT INTO `audit_log` VALUES (2062409003856576515, NULL, 'system', 'TICKET_CLASSIFY', '客服工单分类', 'ticket', 2062409003856576513, NULL, NULL, NULL, NULL, '2026-06-04 13:39:58');
INSERT INTO `audit_log` VALUES (2062409004137594881, NULL, 'system', 'REPORT_GENERATE', '生成报表', 'report_record', 2062409004066291714, NULL, NULL, NULL, NULL, '2026-06-04 13:39:58');
INSERT INTO `audit_log` VALUES (2062409486037983233, NULL, 'system', 'WORKFLOW_START', '启动流程', 'workflow_instance', 2062409485861822467, NULL, NULL, NULL, NULL, '2026-06-04 13:41:53');
INSERT INTO `audit_log` VALUES (2062409486037983234, NULL, 'system', 'CONTRACT_EXTRACT', '合同信息抽取', 'contract_record', 2062409485115236354, NULL, NULL, NULL, NULL, '2026-06-04 13:41:53');
INSERT INTO `audit_log` VALUES (2062409486419664898, NULL, 'system', 'INVOICE_PARSE', '发票解析', 'invoice_record', 2062409486419664897, NULL, NULL, NULL, NULL, '2026-06-04 13:41:53');
INSERT INTO `audit_log` VALUES (2062409486604214276, NULL, 'system', 'TICKET_CLASSIFY', '客服工单分类', 'ticket', 2062409486604214274, NULL, NULL, NULL, NULL, '2026-06-04 13:41:53');
INSERT INTO `audit_log` VALUES (2062409486922981379, NULL, 'system', 'WORKFLOW_START', '启动流程', 'workflow_instance', 2062409486855872514, NULL, NULL, NULL, NULL, '2026-06-04 13:41:54');
INSERT INTO `audit_log` VALUES (2062427545234616322, NULL, 'system', 'SYS_ORG_CREATE', '创建组织', 'sys_org', 2062427544253149185, NULL, NULL, NULL, NULL, '2026-06-04 14:53:39');
INSERT INTO `audit_log` VALUES (2062427545649852417, NULL, 'system', 'RECONCILIATION_CREATE', '创建对账批次', 'reconciliation_batch', 2062427545586937858, NULL, NULL, NULL, NULL, '2026-06-04 14:53:39');
INSERT INTO `audit_log` VALUES (2062427545800847362, NULL, 'system', 'CONNECTOR_CREATE', '创建集成连接器', 'integration_connector', 2062427545800847361, NULL, NULL, NULL, NULL, '2026-06-04 14:53:39');
INSERT INTO `audit_log` VALUES (2062427546148974594, NULL, 'system', 'WEBHOOK_RECEIVE', '接收Webhook事件', 'webhook_event', 2062427546148974593, NULL, NULL, NULL, NULL, '2026-06-04 14:53:39');
INSERT INTO `audit_log` VALUES (2062434238488231938, NULL, 'system', 'KB_QUERY', '知识库问答', 'kb_space', NULL, NULL, NULL, NULL, NULL, '2026-06-04 15:20:15');
INSERT INTO `audit_log` VALUES (2062434321418010626, NULL, 'system', 'REPORT_GENERATE', '生成报表', 'report_record', 2062434321359290369, NULL, NULL, NULL, NULL, '2026-06-04 15:20:35');
INSERT INTO `audit_log` VALUES (2062434359435182082, NULL, 'system', 'WORKFLOW_START', '启动流程', 'workflow_instance', 2062434359372267522, NULL, NULL, NULL, NULL, '2026-06-04 15:20:44');
INSERT INTO `audit_log` VALUES (2062436541958045699, NULL, 'system', 'REPORT_GENERATE', '生成报表', 'report_record', 2062436541958045698, NULL, NULL, NULL, NULL, '2026-06-04 15:29:24');
INSERT INTO `audit_log` VALUES (2062436551927906307, NULL, 'system', 'WORKFLOW_START', '启动流程', 'workflow_instance', 2062436551860797444, NULL, NULL, NULL, NULL, '2026-06-04 15:29:26');
INSERT INTO `audit_log` VALUES (2062436551927906308, NULL, 'system', 'CONTRACT_EXTRACT', '合同信息抽取', 'contract_record', 2062436551797882882, NULL, NULL, NULL, NULL, '2026-06-04 15:29:26');
INSERT INTO `audit_log` VALUES (2062436945760468993, NULL, 'system', 'WORKFLOW_START', '启动流程', 'workflow_instance', 2062436945693360134, NULL, NULL, NULL, NULL, '2026-06-04 15:31:00');
INSERT INTO `audit_log` VALUES (2062436945760468994, NULL, 'system', 'CONTRACT_EXTRACT', '合同信息抽取', 'contract_record', 2062436945693360130, NULL, NULL, NULL, NULL, '2026-06-04 15:31:00');
INSERT INTO `audit_log` VALUES (2062436954383958019, NULL, 'system', 'INVOICE_PARSE', '发票解析', 'invoice_record', 2062436954383958018, NULL, NULL, NULL, NULL, '2026-06-04 15:31:02');
INSERT INTO `audit_log` VALUES (2062436963112304643, NULL, 'system', 'TICKET_CLASSIFY', '客服工单分类', 'ticket', 2062436963112304641, NULL, NULL, NULL, NULL, '2026-06-04 15:31:04');
INSERT INTO `audit_log` VALUES (2062436971668684802, NULL, 'system', 'KB_QUERY', '知识库问答', 'kb_space', NULL, NULL, NULL, NULL, NULL, '2026-06-04 15:31:06');
INSERT INTO `audit_log` VALUES (2062436980384448514, NULL, 'system', 'REPORT_GENERATE', '生成报表', 'report_record', 2062436980384448513, NULL, NULL, NULL, NULL, '2026-06-04 15:31:09');
INSERT INTO `audit_log` VALUES (2062436989087629315, NULL, 'system', 'WORKFLOW_START', '启动流程', 'workflow_instance', 2062436989087629313, NULL, NULL, NULL, NULL, '2026-06-04 15:31:11');
INSERT INTO `audit_log` VALUES (2062437006569488385, NULL, 'system', 'SYS_ORG_CREATE', '创建组织', 'sys_org', 2062437006506573826, NULL, NULL, NULL, NULL, '2026-06-04 15:31:15');
INSERT INTO `audit_log` VALUES (2062437015318806530, NULL, 'system', 'CONNECTOR_CREATE', '创建集成连接器', 'integration_connector', 2062437015255891970, NULL, NULL, NULL, NULL, '2026-06-04 15:31:17');
INSERT INTO `audit_log` VALUES (2062437224849457154, NULL, 'system', 'WEBHOOK_RECEIVE', '接收Webhook事件', 'webhook_event', 2062437224849457153, NULL, NULL, NULL, NULL, '2026-06-04 15:32:07');
INSERT INTO `audit_log` VALUES (2062450456116977666, NULL, 'system', 'FILE_UPLOAD', '上传文件', 'file_asset', 2062450456049868801, NULL, NULL, NULL, NULL, '2026-06-04 16:24:41');
INSERT INTO `audit_log` VALUES (2062450456330887170, NULL, 'system', 'DOC_PARSE_SAVE', '保存文档解析结果', 'doc_parse_result', 2062450456330887169, NULL, NULL, NULL, NULL, '2026-06-04 16:24:41');
INSERT INTO `audit_log` VALUES (2062450456397996037, NULL, 'system', 'FILE_AI_PROCESS', '文件AI解析入库', 'file_asset', 2062450456049868801, NULL, NULL, NULL, NULL, '2026-06-04 16:24:41');
INSERT INTO `audit_log` VALUES (2062450559544320002, NULL, 'system', 'FILE_UPLOAD', '上传文件', 'file_asset', 2062450559481405442, NULL, NULL, NULL, NULL, '2026-06-04 16:25:06');
INSERT INTO `audit_log` VALUES (2062450559544320004, NULL, 'system', 'DOC_PARSE_SAVE', '保存文档解析结果', 'doc_parse_result', 2062450559544320003, NULL, NULL, NULL, NULL, '2026-06-04 16:25:06');
INSERT INTO `audit_log` VALUES (2062450559611428868, NULL, 'system', 'FILE_AI_PROCESS', '文件AI解析入库', 'file_asset', 2062450559481405442, NULL, NULL, NULL, NULL, '2026-06-04 16:25:06');
INSERT INTO `audit_log` VALUES (2062451125146144769, NULL, 'system', 'FILE_UPLOAD', '上传文件', 'file_asset', 2062451125074841601, NULL, NULL, NULL, NULL, '2026-06-04 16:27:21');
INSERT INTO `audit_log` VALUES (2062451125368442882, NULL, 'system', 'DOC_PARSE_SAVE', '保存文档解析结果', 'doc_parse_result', 2062451125368442881, NULL, NULL, NULL, NULL, '2026-06-04 16:27:21');
INSERT INTO `audit_log` VALUES (2062451125439746049, NULL, 'system', 'FILE_AI_PROCESS', '文件AI解析入库', 'file_asset', 2062451125074841601, NULL, NULL, NULL, NULL, '2026-06-04 16:27:21');
INSERT INTO `audit_log` VALUES (2062506470023417858, NULL, 'system', 'FILE_UPLOAD', '上传文件', 'file_asset', 2062506469956308993, NULL, NULL, NULL, NULL, '2026-06-04 20:07:16');
INSERT INTO `audit_log` VALUES (2062506470157635587, NULL, 'system', 'DOC_PARSE_SAVE', '保存文档解析结果', 'doc_parse_result', 2062506470157635586, NULL, NULL, NULL, NULL, '2026-06-04 20:07:16');
INSERT INTO `audit_log` VALUES (2062506470157635589, NULL, 'system', 'FILE_AI_PROCESS', '文件AI解析入库', 'file_asset', 2062506469956308993, NULL, NULL, NULL, NULL, '2026-06-04 20:07:16');
INSERT INTO `audit_log` VALUES (2062508119647375362, NULL, 'system', 'FILE_UPLOAD', '上传文件', 'file_asset', 2062508119567683586, NULL, NULL, NULL, NULL, '2026-06-04 20:13:49');
INSERT INTO `audit_log` VALUES (2062508119647375364, NULL, 'system', 'DOC_PARSE_SAVE', '保存文档解析结果', 'doc_parse_result', 2062508119647375363, NULL, NULL, NULL, NULL, '2026-06-04 20:13:49');
INSERT INTO `audit_log` VALUES (2062508119727067138, NULL, 'system', 'FILE_AI_PROCESS', '文件AI解析入库', 'file_asset', 2062508119567683586, NULL, NULL, NULL, NULL, '2026-06-04 20:13:49');
INSERT INTO `audit_log` VALUES (2062510051224993794, NULL, 'system', 'FILE_UPLOAD', '上传文件', 'file_asset', 2062510051086581761, NULL, NULL, NULL, NULL, '2026-06-04 20:21:30');
INSERT INTO `audit_log` VALUES (2062510051560538115, NULL, 'system', 'DOC_PARSE_SAVE', '保存文档解析结果', 'doc_parse_result', 2062510051560538114, NULL, NULL, NULL, NULL, '2026-06-04 20:21:30');
INSERT INTO `audit_log` VALUES (2062510051627646978, NULL, 'system', 'FILE_AI_PROCESS', '文件AI解析入库', 'file_asset', 2062510051086581761, NULL, NULL, NULL, NULL, '2026-06-04 20:21:30');
INSERT INTO `audit_log` VALUES (2062511074849062914, NULL, 'system', 'FILE_UPLOAD', '上传文件', 'file_asset', 2062511074786148353, NULL, NULL, NULL, NULL, '2026-06-04 20:25:34');
INSERT INTO `audit_log` VALUES (2062511075243327491, NULL, 'system', 'DOC_PARSE_SAVE', '保存文档解析结果', 'doc_parse_result', 2062511075243327490, NULL, NULL, NULL, NULL, '2026-06-04 20:25:34');
INSERT INTO `audit_log` VALUES (2062511075377545218, NULL, 'system', 'FILE_AI_PROCESS', '文件AI解析入库', 'file_asset', 2062511074786148353, NULL, NULL, NULL, NULL, '2026-06-04 20:25:34');
INSERT INTO `audit_log` VALUES (2062511248828792833, NULL, 'system', 'FILE_UPLOAD', '上传文件', 'file_asset', 2062511248702963714, NULL, NULL, NULL, NULL, '2026-06-04 20:26:15');
INSERT INTO `audit_log` VALUES (2062511248963010563, NULL, 'system', 'DOC_PARSE_SAVE', '保存文档解析结果', 'doc_parse_result', 2062511248963010562, NULL, NULL, NULL, NULL, '2026-06-04 20:26:16');
INSERT INTO `audit_log` VALUES (2062511248963010568, NULL, 'system', 'FILE_AI_PROCESS', '文件AI解析入库', 'file_asset', 2062511248702963714, NULL, NULL, NULL, NULL, '2026-06-04 20:26:16');
INSERT INTO `audit_log` VALUES (2062515434152808450, NULL, 'system', 'FILE_UPLOAD', '上传文件', 'file_asset', 2062515434014396417, NULL, NULL, NULL, NULL, '2026-06-04 20:42:53');
INSERT INTO `audit_log` VALUES (2062515434643542018, NULL, 'system', 'DOC_PARSE_SAVE', '保存文档解析结果', 'doc_parse_result', 2062515434643542017, NULL, NULL, NULL, NULL, '2026-06-04 20:42:53');
INSERT INTO `audit_log` VALUES (2062515434643542020, NULL, 'system', 'FILE_AI_PROCESS', '文件AI解析入库', 'file_asset', 2062515434014396417, NULL, NULL, NULL, NULL, '2026-06-04 20:42:53');
INSERT INTO `audit_log` VALUES (2062517708082139138, NULL, 'system', 'FILE_UPLOAD', '上传文件', 'file_asset', 2062517708015030274, NULL, NULL, NULL, NULL, '2026-06-04 20:51:56');
INSERT INTO `audit_log` VALUES (2062517708644175874, NULL, 'system', 'DOC_PARSE_SAVE', '保存文档解析结果', 'doc_parse_result', 2062517708577067009, NULL, NULL, NULL, NULL, '2026-06-04 20:51:56');
INSERT INTO `audit_log` VALUES (2062517708644175876, NULL, 'system', 'FILE_AI_PROCESS', '文件AI解析入库', 'file_asset', 2062517708015030274, NULL, NULL, NULL, NULL, '2026-06-04 20:51:56');
INSERT INTO `audit_log` VALUES (2062519644697161729, NULL, 'system', 'FILE_UPLOAD', '上传文件', 'file_asset', 2062519644575526913, NULL, NULL, NULL, NULL, '2026-06-04 20:59:37');
INSERT INTO `audit_log` VALUES (2062519645401804802, NULL, 'system', 'DOC_PARSE_SAVE', '保存文档解析结果', 'doc_parse_result', 2062519645401804801, NULL, NULL, NULL, NULL, '2026-06-04 20:59:37');
INSERT INTO `audit_log` VALUES (2062519645473107969, NULL, 'system', 'FILE_AI_PROCESS', '文件AI解析入库', 'file_asset', 2062519644575526913, NULL, NULL, NULL, NULL, '2026-06-04 20:59:37');
INSERT INTO `audit_log` VALUES (2062711587574169602, NULL, 'system', 'FILE_UPLOAD', '上传文件', 'file_asset', 2062711587439951873, NULL, NULL, NULL, NULL, '2026-06-05 09:42:20');
INSERT INTO `audit_log` VALUES (2062711587846799364, NULL, 'system', 'DOC_PARSE_SAVE', '保存文档解析结果', 'doc_parse_result', 2062711587846799363, NULL, NULL, NULL, NULL, '2026-06-05 09:42:20');
INSERT INTO `audit_log` VALUES (2062711587976822786, NULL, 'system', 'FILE_AI_PROCESS', '文件AI解析入库', 'file_asset', 2062711587439951873, NULL, NULL, NULL, NULL, '2026-06-05 09:42:20');
INSERT INTO `audit_log` VALUES (2062725745027842050, NULL, 'system', 'FILE_UPLOAD', '上传文件', 'file_asset', 2062725744763600898, NULL, NULL, NULL, NULL, '2026-06-05 10:38:35');
INSERT INTO `audit_log` VALUES (2062725745954783234, NULL, 'system', 'DOC_PARSE_SAVE', '保存文档解析结果', 'doc_parse_result', 2062725745954783233, NULL, NULL, NULL, NULL, '2026-06-05 10:38:36');
INSERT INTO `audit_log` VALUES (2062725746089000962, NULL, 'system', 'FILE_AI_PROCESS', '文件AI解析入库', 'file_asset', 2062725744763600898, NULL, NULL, NULL, NULL, '2026-06-05 10:38:36');
INSERT INTO `audit_log` VALUES (2062726965138386946, NULL, 'system', 'FILE_UPLOAD', '上传文件', 'file_asset', 2062726964958031873, NULL, NULL, NULL, NULL, '2026-06-05 10:43:26');
INSERT INTO `audit_log` VALUES (2062726965603954692, NULL, 'system', 'DOC_PARSE_SAVE', '保存文档解析结果', 'doc_parse_result', 2062726965603954691, NULL, NULL, NULL, NULL, '2026-06-05 10:43:26');
INSERT INTO `audit_log` VALUES (2062726965738172418, NULL, 'system', 'FILE_AI_PROCESS', '文件AI解析入库', 'file_asset', 2062726964958031873, NULL, NULL, NULL, NULL, '2026-06-05 10:43:26');
INSERT INTO `audit_log` VALUES (2062726966262460417, NULL, 'system', 'FILE_UPLOAD', '上传文件', 'file_asset', 2062726966132436993, NULL, NULL, NULL, NULL, '2026-06-05 10:43:27');
INSERT INTO `audit_log` VALUES (2062726966396678147, NULL, 'system', 'DOC_PARSE_SAVE', '保存文档解析结果', 'doc_parse_result', 2062726966396678146, NULL, NULL, NULL, NULL, '2026-06-05 10:43:27');
INSERT INTO `audit_log` VALUES (2062726966463787009, NULL, 'system', 'FILE_AI_PROCESS', '文件AI解析入库', 'file_asset', 2062726966132436993, NULL, NULL, NULL, NULL, '2026-06-05 10:43:27');
INSERT INTO `audit_log` VALUES (2062727191555305474, NULL, 'system', 'FILE_UPLOAD', '上传文件', 'file_asset', 2062727191484002305, NULL, NULL, NULL, NULL, '2026-06-05 10:44:20');
INSERT INTO `audit_log` VALUES (2062727191697911812, NULL, 'system', 'DOC_PARSE_SAVE', '保存文档解析结果', 'doc_parse_result', 2062727191697911811, NULL, NULL, NULL, NULL, '2026-06-05 10:44:20');
INSERT INTO `audit_log` VALUES (2062727191697911814, NULL, 'system', 'FILE_AI_PROCESS', '文件AI解析入库', 'file_asset', 2062727191484002305, NULL, NULL, NULL, NULL, '2026-06-05 10:44:20');
INSERT INTO `audit_log` VALUES (2062727546166931458, NULL, 'system', 'FILE_UPLOAD', '上传文件', 'file_asset', 2062727546099822593, NULL, NULL, NULL, NULL, '2026-06-05 10:45:45');
INSERT INTO `audit_log` VALUES (2062727546305343491, NULL, 'system', 'DOC_PARSE_SAVE', '保存文档解析结果', 'doc_parse_result', 2062727546305343490, NULL, NULL, NULL, NULL, '2026-06-05 10:45:45');
INSERT INTO `audit_log` VALUES (2062727546380840962, NULL, 'system', 'FILE_AI_PROCESS', '文件AI解析入库', 'file_asset', 2062727546099822593, NULL, NULL, NULL, NULL, '2026-06-05 10:45:45');
INSERT INTO `audit_log` VALUES (2062728689974575105, NULL, 'system', 'FILE_UPLOAD', '上传文件', 'file_asset', 2062728689785831425, NULL, NULL, NULL, NULL, '2026-06-05 10:50:18');
INSERT INTO `audit_log` VALUES (2062728690662440963, NULL, 'system', 'DOC_PARSE_SAVE', '保存文档解析结果', 'doc_parse_result', 2062728690662440962, NULL, NULL, NULL, NULL, '2026-06-05 10:50:18');
INSERT INTO `audit_log` VALUES (2062728690792464386, NULL, 'system', 'FILE_AI_PROCESS', '文件AI解析入库', 'file_asset', 2062728689785831425, NULL, NULL, NULL, NULL, '2026-06-05 10:50:18');
INSERT INTO `audit_log` VALUES (2062728819851198465, NULL, 'system', 'FILE_UPLOAD', '上传文件', 'file_asset', 2062728819721175042, NULL, NULL, NULL, NULL, '2026-06-05 10:50:48');
INSERT INTO `audit_log` VALUES (2062728819989610498, NULL, 'system', 'DOC_PARSE_SAVE', '保存文档解析结果', 'doc_parse_result', 2062728819985416194, NULL, NULL, NULL, NULL, '2026-06-05 10:50:48');
INSERT INTO `audit_log` VALUES (2062728819989610500, NULL, 'system', 'FILE_AI_PROCESS', '文件AI解析入库', 'file_asset', 2062728819721175042, NULL, NULL, NULL, NULL, '2026-06-05 10:50:49');
INSERT INTO `audit_log` VALUES (2062729349101060097, NULL, 'system', 'FILE_UPLOAD', '上传文件', 'file_asset', 2062729349029756929, NULL, NULL, NULL, NULL, '2026-06-05 10:52:55');
INSERT INTO `audit_log` VALUES (2062729349314969603, NULL, 'system', 'DOC_PARSE_SAVE', '保存文档解析结果', 'doc_parse_result', 2062729349314969602, NULL, NULL, NULL, NULL, '2026-06-05 10:52:55');
INSERT INTO `audit_log` VALUES (2062729349377884161, NULL, 'system', 'FILE_AI_PROCESS', '文件AI解析入库', 'file_asset', 2062729349029756929, NULL, NULL, NULL, NULL, '2026-06-05 10:52:55');
INSERT INTO `audit_log` VALUES (2062731787816189954, NULL, 'system', 'FILE_FIELDS_CONFIRM', '人工确认文件抽取字段', 'file_asset', 2062729349029756929, NULL, NULL, NULL, NULL, '2026-06-05 11:02:36');
INSERT INTO `audit_log` VALUES (2062732333121847298, NULL, 'system', 'FILE_UPLOAD', '上传文件', 'file_asset', 2062732332920520706, NULL, NULL, NULL, NULL, '2026-06-05 11:04:46');
INSERT INTO `audit_log` VALUES (2062732333541277699, NULL, 'system', 'DOC_PARSE_SAVE', '保存文档解析结果', 'doc_parse_result', 2062732333541277698, NULL, NULL, NULL, NULL, '2026-06-05 11:04:46');
INSERT INTO `audit_log` VALUES (2062732333604192257, NULL, 'system', 'FILE_AI_PROCESS', '文件AI解析入库', 'file_asset', 2062732332920520706, NULL, NULL, NULL, NULL, '2026-06-05 11:04:46');
INSERT INTO `audit_log` VALUES (2062732393591128066, NULL, 'system', 'FILE_FIELDS_CONFIRM', '人工确认文件抽取字段', 'file_asset', 2062732332920520706, NULL, NULL, NULL, NULL, '2026-06-05 11:05:01');
INSERT INTO `audit_log` VALUES (2062732465649270785, NULL, 'system', 'FILE_UPLOAD', '上传文件', 'file_asset', 2062732465506664449, NULL, NULL, NULL, NULL, '2026-06-05 11:05:18');
INSERT INTO `audit_log` VALUES (2062732465716379652, NULL, 'system', 'DOC_PARSE_SAVE', '保存文档解析结果', 'doc_parse_result', 2062732465716379651, NULL, NULL, NULL, NULL, '2026-06-05 11:05:18');
INSERT INTO `audit_log` VALUES (2062732465791877122, NULL, 'system', 'FILE_AI_PROCESS', '文件AI解析入库', 'file_asset', 2062732465506664449, NULL, NULL, NULL, NULL, '2026-06-05 11:05:18');
INSERT INTO `audit_log` VALUES (2062732537074073602, NULL, 'system', 'FILE_FIELDS_CONFIRM', '人工确认文件抽取字段', 'file_asset', 2062732465506664449, NULL, NULL, NULL, NULL, '2026-06-05 11:05:35');
INSERT INTO `audit_log` VALUES (2062739264532234242, NULL, 'system', 'SYS_ROLE_SAVE', '保存角色', 'sys_role', 2062739264532234241, NULL, NULL, NULL, NULL, '2026-06-05 11:32:19');
INSERT INTO `audit_log` VALUES (2062739265194934273, NULL, 'system', 'SYS_USER_SAVE', '保存用户', 'sys_user', 2062739265132019714, NULL, NULL, NULL, NULL, '2026-06-05 11:32:19');
INSERT INTO `audit_log` VALUES (2062739265329152002, NULL, 'system', 'SYS_ROLE_PERMISSION_ASSIGN', '分配角色权限', 'sys_role', 2062739264532234241, NULL, NULL, NULL, NULL, '2026-06-05 11:32:19');
INSERT INTO `audit_log` VALUES (2062739265442398211, NULL, 'system', 'SYS_USER_ROLE_ASSIGN', '分配用户角色', 'sys_user', 2062739265132019714, NULL, NULL, NULL, NULL, '2026-06-05 11:32:19');

-- ----------------------------
-- Table structure for automation_module
-- ----------------------------
DROP TABLE IF EXISTS `automation_module`;
CREATE TABLE `automation_module`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模块编码',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模块名称',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '说明',
  `owner` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '负责部门或角色',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'enabled' COMMENT '状态',
  `sort_order` int(0) NOT NULL DEFAULT 0 COMMENT '排序号',
  `deleted` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_automation_module_code`(`code`) USING BTREE,
  INDEX `idx_automation_module_status`(`status`, `deleted`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业流程自动化能力模块表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of automation_module
-- ----------------------------
INSERT INTO `automation_module` VALUES (1001, 'contract', 'Contract Extraction', 'Extract parties, amount, terms, dates and risks from contracts.', 'Legal/Admin', 'enabled', 10, 0, '2026-06-04 12:28:32', '2026-06-04 12:28:32');
INSERT INTO `automation_module` VALUES (1002, 'invoice', 'Invoice Reconciliation', 'Parse invoices, detect duplicates and reconcile supplier statements.', 'Finance', 'enabled', 20, 0, '2026-06-04 12:28:32', '2026-06-04 12:28:32');
INSERT INTO `automation_module` VALUES (1003, 'ticket', 'Ticket Classification', 'Classify ticket category, priority, sentiment and suggested owner.', 'Customer Service', 'enabled', 30, 0, '2026-06-04 12:28:32', '2026-06-04 12:28:32');
INSERT INTO `automation_module` VALUES (1004, 'sales', 'Sales Follow-up', 'Generate next actions and overdue reminders from customer activities.', 'Sales', 'enabled', 40, 0, '2026-06-04 12:28:32', '2026-06-04 12:28:32');
INSERT INTO `automation_module` VALUES (1005, 'kb', 'Knowledge Base QA', 'Answer questions from enterprise documents with citations.', 'Operations', 'enabled', 50, 0, '2026-06-04 12:28:32', '2026-06-04 12:28:32');
INSERT INTO `automation_module` VALUES (1006, 'report', 'Daily and Weekly Reports', 'Generate progress, risk, todo and operation summaries.', 'Management', 'enabled', 60, 0, '2026-06-04 12:28:32', '2026-06-04 12:28:32');

-- ----------------------------
-- Table structure for contract_record
-- ----------------------------
DROP TABLE IF EXISTS `contract_record`;
CREATE TABLE `contract_record`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `contract_no` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '合同编号',
  `file_id` bigint(0) NULL DEFAULT NULL COMMENT '文件ID',
  `party_a` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '甲方名称',
  `party_b` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '乙方名称',
  `amount` decimal(20, 2) NULL DEFAULT NULL COMMENT '合同金额',
  `currency` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `start_date` date NULL DEFAULT NULL COMMENT '开始日期',
  `end_date` date NULL DEFAULT NULL COMMENT '结束日期',
  `sign_date` date NULL DEFAULT NULL COMMENT '签署日期',
  `payment_terms` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '付款条款',
  `invoice_terms` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '开票条款',
  `renewal_terms` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '续约条款',
  `risk_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'low' COMMENT '风险等级',
  `review_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '复核状态',
  `owner_user_id` bigint(0) NULL DEFAULT NULL COMMENT '归属用户ID',
  `org_id` bigint(0) NULL DEFAULT NULL COMMENT '组织ID',
  `deleted` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_contract_no`(`contract_no`) USING BTREE,
  INDEX `idx_contract_party_a`(`party_a`) USING BTREE,
  INDEX `idx_contract_party_b`(`party_b`) USING BTREE,
  INDEX `idx_contract_end_date`(`end_date`) USING BTREE,
  INDEX `idx_contract_review`(`review_status`, `risk_level`) USING BTREE,
  INDEX `fk_contract_file`(`file_id`) USING BTREE,
  CONSTRAINT `fk_contract_file` FOREIGN KEY (`file_id`) REFERENCES `file_asset` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '合同台账表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of contract_record
-- ----------------------------
INSERT INTO `contract_record` VALUES (2062409485115236354, 'HT-2062409485115236354', NULL, '示例甲方有限公司', '示例乙方有限公司', 128000.00, 'CNY', NULL, NULL, NULL, '验收后45日内付款', NULL, NULL, 'medium', 'pending', NULL, NULL, 0, '2026-06-04 13:41:53', '2026-06-04 13:41:53');
INSERT INTO `contract_record` VALUES (2062436551797882882, 'HT-2062436551797882882', NULL, '示例甲方有限公司', '示例乙方有限公司', 128000.00, 'CNY', NULL, NULL, NULL, '验收后45日内付款', NULL, NULL, 'medium', 'pending', NULL, NULL, 0, '2026-06-04 15:29:26', '2026-06-04 15:29:26');
INSERT INTO `contract_record` VALUES (2062436945693360130, 'HT-2062436945693360130', NULL, '示例甲方有限公司', '示例乙方有限公司', 128000.00, 'CNY', NULL, NULL, NULL, '验收后45日内付款', NULL, NULL, 'medium', 'pending', NULL, NULL, 0, '2026-06-04 15:31:00', '2026-06-04 15:31:00');
INSERT INTO `contract_record` VALUES (2062450456330887171, 'HT-AI-2062450456330887171', 2062450456049868801, '示例甲方有限公司', '示例乙方有限公司', 120000.00, 'CNY', NULL, NULL, NULL, '30天', NULL, NULL, 'medium', 'pending', NULL, NULL, 0, '2026-06-04 16:24:42', '2026-06-04 16:24:42');
INSERT INTO `contract_record` VALUES (2062508119647375365, 'HT-AI-2062508119647375365', 2062508119567683586, '示例甲方有限公司', '示例乙方有限公司', 120000.00, 'CNY', NULL, NULL, NULL, '30天', NULL, NULL, 'medium', 'pending', NULL, NULL, 0, '2026-06-04 20:13:50', '2026-06-04 20:13:50');
INSERT INTO `contract_record` VALUES (2062510051560538116, 'HT-AI-2062510051560538116', 2062510051086581761, '示例甲方有限公司', '示例乙方有限公司', 120000.00, 'CNY', NULL, NULL, NULL, '30天', NULL, NULL, 'medium', 'pending', NULL, NULL, 0, '2026-06-04 20:21:31', '2026-06-04 20:21:31');
INSERT INTO `contract_record` VALUES (2062511075243327492, 'HT-AI-2062511075243327492', 2062511074786148353, '示例甲方有限公司', '示例乙方有限公司', 120000.00, 'CNY', NULL, NULL, NULL, '30天', NULL, NULL, 'medium', 'pending', NULL, NULL, 0, '2026-06-04 20:25:35', '2026-06-04 20:25:35');
INSERT INTO `contract_record` VALUES (2062511248963010564, 'HT-AI-2062511248963010564', 2062511248702963714, '示例甲方有限公司', '示例乙方有限公司', 0.00, 'CNY', NULL, NULL, NULL, '30天', NULL, NULL, 'medium', 'pending', NULL, NULL, 0, '2026-06-04 20:26:16', '2026-06-04 20:26:16');
INSERT INTO `contract_record` VALUES (2062711587846799365, 'HT-AI-2062711587846799365', 2062711587439951873, '示例甲方有限公司', '示例乙方有限公司', 120000.00, 'CNY', NULL, NULL, NULL, '30天', NULL, NULL, 'medium', 'pending', NULL, NULL, 0, '2026-06-05 09:42:21', '2026-06-05 09:42:21');

-- ----------------------------
-- Table structure for contract_reminder
-- ----------------------------
DROP TABLE IF EXISTS `contract_reminder`;
CREATE TABLE `contract_reminder`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `contract_id` bigint(0) NOT NULL COMMENT '合同ID',
  `reminder_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '提醒类型',
  `reminder_time` datetime(0) NOT NULL COMMENT '提醒时间',
  `receiver_user_id` bigint(0) NULL DEFAULT NULL COMMENT '接收用户ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '状态',
  `sent_at` datetime(0) NULL DEFAULT NULL COMMENT '发送时间',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_contract_reminder_contract`(`contract_id`) USING BTREE,
  INDEX `idx_contract_reminder_time`(`reminder_time`, `status`) USING BTREE,
  CONSTRAINT `fk_contract_reminder_contract` FOREIGN KEY (`contract_id`) REFERENCES `contract_record` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '合同提醒表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of contract_reminder
-- ----------------------------

-- ----------------------------
-- Table structure for contract_risk_item
-- ----------------------------
DROP TABLE IF EXISTS `contract_risk_item`;
CREATE TABLE `contract_risk_item`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `contract_id` bigint(0) NOT NULL COMMENT '合同ID',
  `risk_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '风险编码',
  `risk_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '风险名称',
  `risk_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '风险等级',
  `evidence` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '风险依据',
  `suggestion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '处理建议',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'open' COMMENT '状态',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_contract_risk_contract`(`contract_id`) USING BTREE,
  INDEX `idx_contract_risk_level`(`risk_level`, `status`) USING BTREE,
  CONSTRAINT `fk_contract_risk_contract` FOREIGN KEY (`contract_id`) REFERENCES `contract_record` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '合同风险项表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of contract_risk_item
-- ----------------------------
INSERT INTO `contract_risk_item` VALUES (2062409485421420545, 2062409485115236354, 'PAYMENT_TERM_LONG', '付款周期偏长', 'medium', '付款条款包含验收后45日内付款', '建议业务负责人确认现金流影响', 'open', '2026-06-04 13:41:53', '2026-06-04 13:41:53');
INSERT INTO `contract_risk_item` VALUES (2062436551860797441, 2062436551797882882, 'PAYMENT_TERM_LONG', '付款周期偏长', 'medium', '付款条款包含验收后45日内付款', '建议业务负责人确认现金流影响', 'open', '2026-06-04 15:29:26', '2026-06-04 15:29:26');
INSERT INTO `contract_risk_item` VALUES (2062436945693360131, 2062436945693360130, 'PAYMENT_TERM_LONG', '付款周期偏长', 'medium', '付款条款包含验收后45日内付款', '建议业务负责人确认现金流影响', 'open', '2026-06-04 15:31:00', '2026-06-04 15:31:00');
INSERT INTO `contract_risk_item` VALUES (2062450456397996034, 2062450456330887171, 'AI_RISK', '金额超过10万元，建议进入人工复核。', 'medium', '金额超过10万元，建议进入人工复核。', '请业务负责人和法务复核该风险点', 'open', '2026-06-04 16:24:41', '2026-06-04 16:24:41');
INSERT INTO `contract_risk_item` VALUES (2062508119647375366, 2062508119647375365, 'AI_RISK', '金额超过10万元，建议进入人工复核。', 'medium', '金额超过10万元，建议进入人工复核。', '请业务负责人和法务复核该风险点', 'open', '2026-06-04 20:13:49', '2026-06-04 20:13:49');
INSERT INTO `contract_risk_item` VALUES (2062510051560538117, 2062510051560538116, 'AI_RISK', '金额超过10万元，建议进入人工复核。', 'medium', '金额超过10万元，建议进入人工复核。', '请业务负责人和法务复核该风险点', 'open', '2026-06-04 20:21:30', '2026-06-04 20:21:30');
INSERT INTO `contract_risk_item` VALUES (2062511075310436354, 2062511075243327492, 'AI_RISK', '金额超过10万元，建议进入人工复核。', 'medium', '金额超过10万元，建议进入人工复核。', '请业务负责人和法务复核该风险点', 'open', '2026-06-04 20:25:34', '2026-06-04 20:25:34');
INSERT INTO `contract_risk_item` VALUES (2062511248963010565, 2062511248963010564, 'AI_RISK', '金额超过10万元，建议进入人工复核。', 'medium', '金额超过10万元，建议进入人工复核。', '请业务负责人和法务复核该风险点', 'open', '2026-06-04 20:26:16', '2026-06-04 20:26:16');
INSERT INTO `contract_risk_item` VALUES (2062711587913908225, 2062711587846799365, 'AI_RISK', '金额超过10万元，建议进入人工复核。', 'medium', '金额超过10万元，建议进入人工复核。', '请业务负责人和法务复核该风险点', 'open', '2026-06-05 09:42:20', '2026-06-05 09:42:20');

-- ----------------------------
-- Table structure for doc_parse_result
-- ----------------------------
DROP TABLE IF EXISTS `doc_parse_result`;
CREATE TABLE `doc_parse_result`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `file_id` bigint(0) NOT NULL COMMENT '文件ID',
  `page_no` int(0) NOT NULL DEFAULT 1 COMMENT '页码',
  `raw_text` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '原始识别文本',
  `layout_json` json NULL COMMENT '版面解析JSON',
  `ocr_engine` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'OCR引擎',
  `confidence` decimal(5, 4) NOT NULL COMMENT '置信度',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'completed' COMMENT '状态',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_doc_parse_file`(`file_id`, `page_no`) USING BTREE,
  CONSTRAINT `fk_doc_parse_file` FOREIGN KEY (`file_id`) REFERENCES `file_asset` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文档解析结果表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of doc_parse_result
-- ----------------------------
INSERT INTO `doc_parse_result` VALUES (2062450456330887169, 2062450456049868801, 1, '????120000??????30????????', NULL, 'manual', 1.0000, 'completed', '2026-06-04 16:24:41');
INSERT INTO `doc_parse_result` VALUES (2062450559544320003, 2062450559481405442, 1, 'åç¥¨å·ç FP20260604001ï¼è½¯ä»¶æå¡è´¹8600åï¼ç¨ç6%ã', NULL, 'manual', 1.0000, 'completed', '2026-06-04 16:25:06');
INSERT INTO `doc_parse_result` VALUES (2062451125368442881, 2062451125074841601, 1, '????FP20260604001??????8600????6%?', NULL, 'manual', 1.0000, 'completed', '2026-06-04 16:27:21');
INSERT INTO `doc_parse_result` VALUES (2062506470157635586, 2062506469956308993, 1, '发票号码FP20260604001，软件服务费8600元，税率6%。', NULL, 'manual', 1.0000, 'completed', '2026-06-04 20:07:16');
INSERT INTO `doc_parse_result` VALUES (2062508119647375363, 2062508119567683586, 1, '合同金额120000元，付款周期30天。', NULL, 'manual', 1.0000, 'completed', '2026-06-04 20:13:49');
INSERT INTO `doc_parse_result` VALUES (2062510051560538114, 2062510051086581761, 1, '合同编号HT202606041234567890，项目编号2062508119567683586，合同金额120000元，付款周期30天。', NULL, 'manual', 1.0000, 'completed', '2026-06-04 20:21:30');
INSERT INTO `doc_parse_result` VALUES (2062511075243327490, 2062511074786148353, 1, '合同编号HT202606041234567890，项目编号2062508119567683586，合同金额120000元，付款周期30天。', NULL, 'manual', 1.0000, 'completed', '2026-06-04 20:25:34');
INSERT INTO `doc_parse_result` VALUES (2062511248963010562, 2062511248702963714, 1, '电子发票（普通发票） 发票号码：\n开票日期：\n销\n售\n方\n信\n息\n统一社会信用代码/纳税人识别号：\n名称：购\n买\n方\n信\n息\n统一社会信用代码/纳税人识别号：\n名称：\n26317000001494503286\n2026年04月29日\n浙江毅星科技有限公司成都分公司\n91510100MAE8RKCX8J\n上海华程西南国际旅行社有限公司\n91310105134638405A\n项目名称 规格型号 单  位 数  量 单  价 金  额 税率/征收率 税  额\n*经纪代理服务*代订机票 1 1094.339622641509 1094.34 6% 65.66\n产品\n价税合计（大写）\n合        计\n（小写）\n备\n注\n开票人：\n壹仟壹佰陆拾圆整 ¥1160.00\n韩贇斐\n¥1094.34 ¥65.66\n携程订单:1128147306809855,2026/4/20 成都-上海 MU9188 伍汭 经济舱', NULL, 'manual', 1.0000, 'completed', '2026-06-04 20:26:16');
INSERT INTO `doc_parse_result` VALUES (2062515434643542017, 2062515434014396417, 1, '电子发票（普通发票） 发票号码：\n开票日期：\n销\n售\n方\n信\n息\n统一社会信用代码/纳税人识别号：\n名称：购\n买\n方\n信\n息\n统一社会信用代码/纳税人识别号：\n名称：\n26317000001494503286\n2026年04月29日\n浙江毅星科技有限公司成都分公司\n91510100MAE8RKCX8J\n上海华程西南国际旅行社有限公司\n91310105134638405A\n项目名称 规格型号 单  位 数  量 单  价 金  额 税率/征收率 税  额\n*经纪代理服务*代订机票 1 1094.339622641509 1094.34 6% 65.66\n产品\n价税合计（大写）\n合        计\n（小写）\n备\n注\n开票人：\n壹仟壹佰陆拾圆整 ¥1160.00\n韩贇斐\n¥1094.34 ¥65.66\n携程订单:1128147306809855,2026/4/20 成都-上海 MU9188 伍汭 经济舱', NULL, 'manual', 1.0000, 'completed', '2026-06-04 20:42:53');
INSERT INTO `doc_parse_result` VALUES (2062517708577067009, 2062517708015030274, 1, '电子发票（普通发票） 发票号码：\n开票日期：\n销\n售\n方\n信\n息\n统一社会信用代码/纳税人识别号：\n名称：购\n买\n方\n信\n息\n统一社会信用代码/纳税人识别号：\n名称：\n26317000001494503286\n2026年04月29日\n浙江毅星科技有限公司成都分公司\n91510100MAE8RKCX8J\n上海华程西南国际旅行社有限公司\n91310105134638405A\n项目名称 规格型号 单  位 数  量 单  价 金  额 税率/征收率 税  额\n*经纪代理服务*代订机票 1 1094.339622641509 1094.34 6% 65.66\n产品\n价税合计（大写）\n合        计\n（小写）\n备\n注\n开票人：\n壹仟壹佰陆拾圆整 ¥1160.00\n韩贇斐\n¥1094.34 ¥65.66\n携程订单:1128147306809855,2026/4/20 成都-上海 MU9188 伍汭 经济舱', NULL, 'manual', 1.0000, 'completed', '2026-06-04 20:51:56');
INSERT INTO `doc_parse_result` VALUES (2062519645401804801, 2062519644575526913, 1, '电子发票（普通发票） 发票号码：\n开票日期：\n销\n售\n方\n信\n息\n统一社会信用代码/纳税人识别号：\n名称：购\n买\n方\n信\n息\n统一社会信用代码/纳税人识别号：\n名称：\n26317000001494503286\n2026年04月29日\n浙江毅星科技有限公司成都分公司\n91510100MAE8RKCX8J\n上海华程西南国际旅行社有限公司\n91310105134638405A\n项目名称 规格型号 单  位 数  量 单  价 金  额 税率/征收率 税  额\n*经纪代理服务*代订机票 1 1094.339622641509 1094.34 6% 65.66\n产品\n价税合计（大写）\n合        计\n（小写）\n备\n注\n开票人：\n壹仟壹佰陆拾圆整 ¥1160.00\n韩贇斐\n¥1094.34 ¥65.66\n携程订单:1128147306809855,2026/4/20 成都-上海 MU9188 伍汭 经济舱', NULL, 'manual', 1.0000, 'completed', '2026-06-04 20:59:37');
INSERT INTO `doc_parse_result` VALUES (2062711587846799363, 2062711587439951873, 1, '合同金额120000元，付款周期30天。', NULL, 'manual', 1.0000, 'completed', '2026-06-05 09:42:20');
INSERT INTO `doc_parse_result` VALUES (2062725745954783233, 2062725744763600898, 1, '电子发票（普通发票） 发票号码：\n开票日期：\n销\n售\n方\n信\n息\n统一社会信用代码/纳税人识别号：\n名称：购\n买\n方\n信\n息\n统一社会信用代码/纳税人识别号：\n名称：\n26317000001494503286\n2026年04月29日\n浙江毅星科技有限公司成都分公司\n91510100MAE8RKCX8J\n上海华程西南国际旅行社有限公司\n91310105134638405A\n项目名称 规格型号 单  位 数  量 单  价 金  额 税率/征收率 税  额\n*经纪代理服务*代订机票 1 1094.339622641509 1094.34 6% 65.66\n产品\n价税合计（大写）\n合        计\n（小写）\n备\n注\n开票人：\n壹仟壹佰陆拾圆整 ¥1160.00\n韩贇斐\n¥1094.34 ¥65.66\n携程订单:1128147306809855,2026/4/20 成都-上海 MU9188 伍汭 经济舱', NULL, 'manual', 1.0000, 'completed', '2026-06-05 10:38:36');
INSERT INTO `doc_parse_result` VALUES (2062726965603954691, 2062726964958031873, 1, '电子发票（普通发票） 发票号码：\n开票日期：\n销\n售\n方\n信\n息\n统一社会信用代码/纳税人识别号：\n名称：购\n买\n方\n信\n息\n统一社会信用代码/纳税人识别号：\n名称：\n26317000001494503286\n2026年04月29日\n浙江毅星科技有限公司成都分公司\n91510100MAE8RKCX8J\n上海华程西南国际旅行社有限公司\n91310105134638405A\n项目名称 规格型号 单  位 数  量 单  价 金  额 税率/征收率 税  额\n*经纪代理服务*代订机票 1 1094.339622641509 1094.34 6% 65.66\n产品\n价税合计（大写）\n合        计\n（小写）\n备\n注\n开票人：\n壹仟壹佰陆拾圆整 ¥1160.00\n韩贇斐\n¥1094.34 ¥65.66\n携程订单:1128147306809855,2026/4/20 成都-上海 MU9188 伍汭 经济舱', NULL, 'manual', 1.0000, 'completed', '2026-06-05 10:43:26');
INSERT INTO `doc_parse_result` VALUES (2062726966396678146, 2062726966132436993, 1, '电子发票（普通发票） 发票号码：\n开票日期：\n销\n售\n方\n信\n息\n统一社会信用代码/纳税人识别号：\n名称：购\n买\n方\n信\n息\n统一社会信用代码/纳税人识别号：\n名称：\n26317000001494503286\n2026年04月29日\n浙江毅星科技有限公司成都分公司\n91510100MAE8RKCX8J\n上海华程西南国际旅行社有限公司\n91310105134638405A\n项目名称 规格型号 单  位 数  量 单  价 金  额 税率/征收率 税  额\n*经纪代理服务*代订机票 1 1094.339622641509 1094.34 6% 65.66\n产品\n价税合计（大写）\n合        计\n（小写）\n备\n注\n开票人：\n壹仟壹佰陆拾圆整 ¥1160.00\n韩贇斐\n¥1094.34 ¥65.66\n携程订单:1128147306809855,2026/4/20 成都-上海 MU9188 伍汭 经济舱', NULL, 'manual', 1.0000, 'completed', '2026-06-05 10:43:27');
INSERT INTO `doc_parse_result` VALUES (2062727191697911811, 2062727191484002305, 1, '电子发票（普通发票） 发票号码：\n开票日期：\n销\n售\n方\n信\n息\n统一社会信用代码/纳税人识别号：\n名称：购\n买\n方\n信\n息\n统一社会信用代码/纳税人识别号：\n名称：\n26317000001494503286\n2026年04月29日\n浙江毅星科技有限公司成都分公司\n91510100MAE8RKCX8J\n上海华程西南国际旅行社有限公司\n91310105134638405A\n项目名称 规格型号 单  位 数  量 单  价 金  额 税率/征收率 税  额\n*经纪代理服务*代订机票 1 1094.339622641509 1094.34 6% 65.66\n产品\n价税合计（大写）\n合        计\n（小写）\n备\n注\n开票人：\n壹仟壹佰陆拾圆整 ¥1160.00\n韩贇斐\n¥1094.34 ¥65.66\n携程订单:1128147306809855,2026/4/20 成都-上海 MU9188 伍汭 经济舱', NULL, 'manual', 1.0000, 'completed', '2026-06-05 10:44:20');
INSERT INTO `doc_parse_result` VALUES (2062727546305343490, 2062727546099822593, 1, '电子发票（普通发票） 发票号码：\n开票日期：\n销\n售\n方\n信\n息\n统一社会信用代码/纳税人识别号：\n名称：购\n买\n方\n信\n息\n统一社会信用代码/纳税人识别号：\n名称：\n26317000001494503286\n2026年04月29日\n浙江毅星科技有限公司成都分公司\n91510100MAE8RKCX8J\n上海华程西南国际旅行社有限公司\n91310105134638405A\n项目名称 规格型号 单  位 数  量 单  价 金  额 税率/征收率 税  额\n*经纪代理服务*代订机票 1 1094.339622641509 1094.34 6% 65.66\n产品\n价税合计（大写）\n合        计\n（小写）\n备\n注\n开票人：\n壹仟壹佰陆拾圆整 ¥1160.00\n韩贇斐\n¥1094.34 ¥65.66\n携程订单:1128147306809855,2026/4/20 成都-上海 MU9188 伍汭 经济舱', NULL, 'manual', 1.0000, 'completed', '2026-06-05 10:45:45');
INSERT INTO `doc_parse_result` VALUES (2062728690662440962, 2062728689785831425, 1, '电子发票（普通发票） 发票号码：\n开票日期：\n销\n售\n方\n信\n息\n统一社会信用代码/纳税人识别号：\n名称：购\n买\n方\n信\n息\n统一社会信用代码/纳税人识别号：\n名称：\n26317000001494503286\n2026年04月29日\n浙江毅星科技有限公司成都分公司\n91510100MAE8RKCX8J\n上海华程西南国际旅行社有限公司\n91310105134638405A\n项目名称 规格型号 单  位 数  量 单  价 金  额 税率/征收率 税  额\n*经纪代理服务*代订机票 1 1094.339622641509 1094.34 6% 65.66\n产品\n价税合计（大写）\n合        计\n（小写）\n备\n注\n开票人：\n壹仟壹佰陆拾圆整 ¥1160.00\n韩贇斐\n¥1094.34 ¥65.66\n携程订单:1128147306809855,2026/4/20 成都-上海 MU9188 伍汭 经济舱', '{\"extraction\": {\"risks\": [], \"fields\": {\"amount\": \"1160.00\", \"party_a\": \"示例甲方有限公司\", \"party_b\": \"示例乙方有限公司\", \"order_no\": \"1128147306809855\", \"scenario\": \"invoice\", \"tax_rate\": \"6%\", \"buyer_name\": \"上海华程西南国际旅行社有限公司\", \"invoice_no\": \"26317000001494503286\", \"tax_amount\": \"65.66\", \"flight_info\": \"2026/4/20 成都-上海 MU9188\", \"line_amount\": \"1094.34\", \"seller_name\": \"浙江毅星科技有限公司成都分公司\", \"buyer_tax_no\": \"91310105134638405A\", \"invoice_code\": \"26317000001494503286\", \"invoice_date\": \"2026-04-29\", \"total_amount\": \"1160.00\", \"payment_terms\": \"30天\", \"seller_tax_no\": \"91510100MAE8RKCX8J\"}, \"scenario\": \"invoice\", \"confidence\": 0.95, \"reviewRequired\": false}}', 'manual', 1.0000, 'completed', '2026-06-05 10:50:18');
INSERT INTO `doc_parse_result` VALUES (2062728819985416194, 2062728819721175042, 1, '电子发票（普通发票） 发票号码：\n开票日期：\n销\n售\n方\n信\n息\n统一社会信用代码/纳税人识别号：\n名称：购\n买\n方\n信\n息\n统一社会信用代码/纳税人识别号：\n名称：\n26317000001494503286\n2026年04月29日\n浙江毅星科技有限公司成都分公司\n91510100MAE8RKCX8J\n上海华程西南国际旅行社有限公司\n91310105134638405A\n项目名称 规格型号 单  位 数  量 单  价 金  额 税率/征收率 税  额\n*经纪代理服务*代订机票 1 1094.339622641509 1094.34 6% 65.66\n产品\n价税合计（大写）\n合        计\n（小写）\n备\n注\n开票人：\n壹仟壹佰陆拾圆整 ¥1160.00\n韩贇斐\n¥1094.34 ¥65.66\n携程订单:1128147306809855,2026/4/20 成都-上海 MU9188 伍汭 经济舱', '{\"extraction\": {\"risks\": [], \"fields\": {\"amount\": \"1160.00\", \"party_a\": \"示例甲方有限公司\", \"party_b\": \"示例乙方有限公司\", \"order_no\": \"1128147306809855\", \"scenario\": \"invoice\", \"tax_rate\": \"6%\", \"buyer_name\": \"上海华程西南国际旅行社有限公司\", \"invoice_no\": \"26317000001494503286\", \"tax_amount\": \"65.66\", \"flight_info\": \"2026/4/20 成都-上海 MU9188\", \"line_amount\": \"1094.34\", \"seller_name\": \"浙江毅星科技有限公司成都分公司\", \"buyer_tax_no\": \"91310105134638405A\", \"invoice_code\": \"26317000001494503286\", \"invoice_date\": \"2026-04-29\", \"total_amount\": \"1160.00\", \"payment_terms\": \"30天\", \"seller_tax_no\": \"91510100MAE8RKCX8J\"}, \"scenario\": \"invoice\", \"confidence\": 0.95, \"reviewRequired\": false}}', 'manual', 1.0000, 'completed', '2026-06-05 10:50:48');
INSERT INTO `doc_parse_result` VALUES (2062729349314969602, 2062729349029756929, 1, '开票人： 于秋红\ndidi\n电子发票（普通发票）\n旅客运输服务 发票号码 : 26317000001494790709\n开票日期 : 2026年04月29日\n备\n注\n销\n售\n方\n信\n息\n购\n买\n方\n信\n息\n名称： 浙江毅星科技有限公司成都分公司\n统一社会信用代码/纳税人识别号： 91510100MAE8RKCX8J\n名称： 上海滴滴畅行科技有限公司\n统一社会信用代码/纳税人识别号： 91310114MA1GW61J6U\n项目名称 单  价 数  量 金  额 税率/征收率 税  额\n*运输服务*客运服务费 255.14 1 255.14 3% 7.65\n合 计 255.14¥ 7.65¥\n出行人 有效身份证件号 出行日期 出发地 到达地 等级 交通工具类型\n价 税 合 计 （ 大 写 ） （ 小 写 ） 262.79¥贰佰陆拾贰圆柒角玖分', '{\"extraction\": {\"risks\": [\"人工已确认字段\"], \"fields\": {\"amount\": \"7.65\", \"party_a\": \"ç¤ºä¾ç²æ¹æéå¬å¸\", \"party_b\": \"ç¤ºä¾ä¹æ¹æéå¬å¸\", \"order_no\": \"\", \"scenario\": \"invoice\", \"tax_rate\": \"3%\", \"buyer_name\": \"人工确认购买方有限公司\", \"invoice_no\": \"26317000001494790709\", \"tax_amount\": \"0.00\", \"flight_info\": \"\", \"line_amount\": \"7.65\", \"seller_name\": \"æµæ±æ¯æç§ææéå¬å¸æé½åå¬å¸\", \"buyer_tax_no\": \"91310114MA1GW61J6U\", \"invoice_code\": \"26317000001494790709\", \"invoice_date\": \"2026-04-29\", \"total_amount\": \"7.65\", \"payment_terms\": \"å¾ç¡®è®¤\", \"seller_tax_no\": \"91510100MAE8RKCX8J\"}, \"scenario\": \"invoice\", \"confidence\": 1.0, \"reviewRequired\": false}}', 'manual', 1.0000, 'completed', '2026-06-05 10:52:55');
INSERT INTO `doc_parse_result` VALUES (2062732333541277698, 2062732332920520706, 1, '电子发票（普通发票） 发票号码：\n开票日期：\n销\n售\n方\n信\n息\n统一社会信用代码/纳税人识别号：\n名称：购\n买\n方\n信\n息\n统一社会信用代码/纳税人识别号：\n名称：\n26317000001494503286\n2026年04月29日\n浙江毅星科技有限公司成都分公司\n91510100MAE8RKCX8J\n上海华程西南国际旅行社有限公司\n91310105134638405A\n项目名称 规格型号 单  位 数  量 单  价 金  额 税率/征收率 税  额\n*经纪代理服务*代订机票 1 1094.339622641509 1094.34 6% 65.66\n产品\n价税合计（大写）\n合        计\n（小写）\n备\n注\n开票人：\n壹仟壹佰陆拾圆整 ¥1160.00\n韩贇斐\n¥1094.34 ¥65.66\n携程订单:1128147306809855,2026/4/20 成都-上海 MU9188 伍汭 经济舱', '{\"extraction\": {\"risks\": [\"人工已确认字段\"], \"fields\": {\"amount\": \"1160.00\", \"party_a\": \"示例甲方有限公司\", \"party_b\": \"示例乙方有限公司\", \"order_no\": \"1128147306809855\", \"scenario\": \"invoice\", \"tax_rate\": \"6%\", \"buyer_name\": \"上海华程西南国际旅行社有限公司\", \"invoice_no\": \"26317000001494503286\", \"tax_amount\": \"65.66\", \"flight_info\": \"2026/4/20 成都-上海 MU9188\", \"line_amount\": \"1094.34\", \"seller_name\": \"浙江毅星科技有限公司成都分公司\", \"buyer_tax_no\": \"91310105134638405A\", \"invoice_code\": \"26317000001494503286\", \"invoice_date\": \"2026-04-29\", \"total_amount\": \"1160.00\", \"payment_terms\": \"30天\", \"seller_tax_no\": \"91510100MAE8RKCX8J\"}, \"scenario\": \"invoice\", \"confidence\": 1.0, \"reviewRequired\": false}}', 'manual', 1.0000, 'completed', '2026-06-05 11:04:46');
INSERT INTO `doc_parse_result` VALUES (2062732465716379651, 2062732465506664449, 1, '开票人： 于秋红\ndidi\n电子发票（普通发票）\n旅客运输服务 发票号码 : 26317000001494790709\n开票日期 : 2026年04月29日\n备\n注\n销\n售\n方\n信\n息\n购\n买\n方\n信\n息\n名称： 浙江毅星科技有限公司成都分公司\n统一社会信用代码/纳税人识别号： 91510100MAE8RKCX8J\n名称： 上海滴滴畅行科技有限公司\n统一社会信用代码/纳税人识别号： 91310114MA1GW61J6U\n项目名称 单  价 数  量 金  额 税率/征收率 税  额\n*运输服务*客运服务费 255.14 1 255.14 3% 7.65\n合 计 255.14¥ 7.65¥\n出行人 有效身份证件号 出行日期 出发地 到达地 等级 交通工具类型\n价 税 合 计 （ 大 写 ） （ 小 写 ） 262.79¥贰佰陆拾贰圆柒角玖分', '{\"extraction\": {\"risks\": [\"人工已确认字段\"], \"fields\": {\"amount\": \"7.65\", \"party_a\": \"示例甲方有限公司\", \"party_b\": \"示例乙方有限公司\", \"order_no\": \"\", \"scenario\": \"invoice\", \"tax_rate\": \"3%\", \"buyer_name\": \"上海滴滴畅行科技有限公司\", \"invoice_no\": \"26317000001494790709\", \"tax_amount\": \"0.00\", \"flight_info\": \"\", \"line_amount\": \"7.65\", \"seller_name\": \"浙江毅星科技有限公司成都分公司\", \"buyer_tax_no\": \"91310114MA1GW61J6U\", \"invoice_code\": \"26317000001494790709\", \"invoice_date\": \"2026-04-29\", \"total_amount\": \"7.65\", \"payment_terms\": \"待确认\", \"seller_tax_no\": \"91510100MAE8RKCX8J\"}, \"scenario\": \"invoice\", \"confidence\": 1.0, \"reviewRequired\": false}}', 'manual', 1.0000, 'completed', '2026-06-05 11:05:18');

-- ----------------------------
-- Table structure for file_asset
-- ----------------------------
DROP TABLE IF EXISTS `file_asset`;
CREATE TABLE `file_asset`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '存储文件名',
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原始文件名',
  `file_ext` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件扩展名',
  `mime_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件MIME类型',
  `file_size` bigint(0) NOT NULL DEFAULT 0 COMMENT '文件大小，单位字节',
  `file_hash` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件哈希值',
  `storage_bucket` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '对象存储桶名称',
  `storage_key` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '对象存储键',
  `business_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型',
  `owner_user_id` bigint(0) NULL DEFAULT NULL COMMENT '归属用户ID',
  `org_id` bigint(0) NULL DEFAULT NULL COMMENT '组织ID',
  `parse_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '解析状态',
  `deleted` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_file_asset_business`(`business_type`, `parse_status`) USING BTREE,
  INDEX `idx_file_asset_owner`(`owner_user_id`) USING BTREE,
  INDEX `idx_file_asset_org`(`org_id`) USING BTREE,
  INDEX `idx_file_asset_hash`(`file_hash`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件资产表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_asset
-- ----------------------------
INSERT INTO `file_asset` VALUES (2062450456049868801, '2062450456049868801.txt', 'contract.txt', 'txt', 'text/plain', 26, '3aeeea63ef95b3c8908c0636729647b3', 'local', 'storage\\uploads\\2062450456049868801.txt', 'contract', NULL, NULL, 'completed', 0, '2026-06-04 16:24:41', '2026-06-04 16:24:41');
INSERT INTO `file_asset` VALUES (2062450559481405442, '2062450559481405442.txt', 'invoice.txt', 'txt', 'text/plain', 109, 'ca5259cf80f30c2564ee9601f7eae41f', 'local', 'storage\\uploads\\2062450559481405442.txt', 'invoice', NULL, NULL, 'completed', 0, '2026-06-04 16:25:06', '2026-06-04 16:25:06');
INSERT INTO `file_asset` VALUES (2062451125074841601, '2062451125074841601.txt', 'invoice.txt', 'txt', 'text/plain', 34, 'cb2552c28842515ad225222859c9fa24', 'local', 'storage\\uploads\\2062451125074841601.txt', 'invoice', NULL, NULL, 'completed', 0, '2026-06-04 16:27:21', '2026-06-04 16:27:21');
INSERT INTO `file_asset` VALUES (2062506469956308993, '2062506469956308993.txt', 'invoice.txt', 'txt', 'text/plain', 64, 'd9dc206cb60a6bdb7079dcc47258cb6c', 'local', 'storage\\uploads\\2062506469956308993.txt', 'invoice', NULL, NULL, 'completed', 0, '2026-06-04 20:07:16', '2026-06-04 20:07:16');
INSERT INTO `file_asset` VALUES (2062508119567683586, '2062508119567683586.txt', 'qyglai-upload-test.txt', 'txt', 'text/plain', 49, '4be7534b8cafbe0ec1602ffab0a35694', 'local', 'storage\\uploads\\2062508119567683586.txt', 'contract', NULL, NULL, 'completed', 0, '2026-06-04 20:13:49', '2026-06-04 20:13:49');
INSERT INTO `file_asset` VALUES (2062510051086581761, '2062510051086581761.txt', 'qyglai-overflow-contract.txt', 'txt', 'text/plain', 118, '3526df79173b15069a484948ead5a198', 'local', 'storage\\uploads\\2062510051086581761.txt', 'contract', NULL, NULL, 'completed', 0, '2026-06-04 20:21:30', '2026-06-04 20:21:30');
INSERT INTO `file_asset` VALUES (2062511074786148353, '2062511074786148353.txt', 'qyglai-overflow-contract-final.txt', 'txt', 'text/plain', 118, '3526df79173b15069a484948ead5a198', 'local', 'storage\\uploads\\2062511074786148353.txt', 'contract', NULL, NULL, 'completed', 0, '2026-06-04 20:25:34', '2026-06-04 20:25:34');
INSERT INTO `file_asset` VALUES (2062511248702963714, '2062511248702963714.pdf', '机票预订出行订单尾号06809855-电子普通发票.pdf', 'pdf', 'application/pdf', 63138, 'f9ab91d5d495d7d053e59c2df82158ab', 'local', 'storage\\uploads\\2062511248702963714.pdf', 'contract', NULL, NULL, 'completed', 0, '2026-06-04 20:26:15', '2026-06-04 20:26:16');
INSERT INTO `file_asset` VALUES (2062515434014396417, '2062515434014396417.pdf', '机票预订出行订单尾号06809855-电子普通发票.pdf', 'pdf', 'application/pdf', 63138, 'f9ab91d5d495d7d053e59c2df82158ab', 'local', 'storage\\uploads\\2062515434014396417.pdf', 'invoice', NULL, NULL, 'completed', 0, '2026-06-04 20:42:53', '2026-06-04 20:42:53');
INSERT INTO `file_asset` VALUES (2062517708015030274, '2062517708015030274.pdf', '机票预订出行订单尾号06809855-电子普通发票.pdf', 'pdf', 'application/pdf', 63138, 'f9ab91d5d495d7d053e59c2df82158ab', 'local', 'storage\\uploads\\2062517708015030274.pdf', 'invoice', NULL, NULL, 'completed', 0, '2026-06-04 20:51:56', '2026-06-04 20:51:56');
INSERT INTO `file_asset` VALUES (2062519644575526913, '2062519644575526913.pdf', '机票预订出行订单尾号06809855-电子普通发票.pdf', 'pdf', 'application/pdf', 63138, 'f9ab91d5d495d7d053e59c2df82158ab', 'local', 'storage\\uploads\\2062519644575526913.pdf', 'invoice', NULL, NULL, 'completed', 0, '2026-06-04 20:59:37', '2026-06-04 20:59:37');
INSERT INTO `file_asset` VALUES (2062711587439951873, '2062711587439951873.txt', 'ai-log-contract.txt', 'txt', 'text/plain', 49, '4be7534b8cafbe0ec1602ffab0a35694', 'local', 'storage\\uploads\\2062711587439951873.txt', 'contract', NULL, NULL, 'completed', 0, '2026-06-05 09:42:20', '2026-06-05 09:42:20');
INSERT INTO `file_asset` VALUES (2062725744763600898, '2062725744763600898.pdf', '机票预订出行订单尾号06809855-电子普通发票.pdf', 'pdf', 'application/pdf', 63138, 'f9ab91d5d495d7d053e59c2df82158ab', 'local', 'storage\\uploads\\2062725744763600898.pdf', 'invoice', NULL, NULL, 'completed', 0, '2026-06-05 10:38:35', '2026-06-05 10:38:36');
INSERT INTO `file_asset` VALUES (2062726964958031873, '2062726964958031873.pdf', '机票预订出行订单尾号06809855-电子普通发票.pdf', 'pdf', 'application/pdf', 63138, 'f9ab91d5d495d7d053e59c2df82158ab', 'local', 'storage\\uploads\\2062726964958031873.pdf', 'invoice', NULL, NULL, 'completed', 0, '2026-06-05 10:43:26', '2026-06-05 10:43:26');
INSERT INTO `file_asset` VALUES (2062726966132436993, '2062726966132436993.pdf', '机票预订出行订单尾号06809855-电子普通发票.pdf', 'pdf', 'application/pdf', 63138, 'f9ab91d5d495d7d053e59c2df82158ab', 'local', 'storage\\uploads\\2062726966132436993.pdf', 'invoice', NULL, NULL, 'completed', 0, '2026-06-05 10:43:27', '2026-06-05 10:43:27');
INSERT INTO `file_asset` VALUES (2062727191484002305, '2062727191484002305.pdf', '机票预订出行订单尾号06809855-电子普通发票.pdf', 'pdf', 'application/pdf', 63138, 'f9ab91d5d495d7d053e59c2df82158ab', 'local', 'storage\\uploads\\2062727191484002305.pdf', 'invoice', NULL, NULL, 'completed', 0, '2026-06-05 10:44:20', '2026-06-05 10:44:20');
INSERT INTO `file_asset` VALUES (2062727546099822593, '2062727546099822593.pdf', '机票预订出行订单尾号06809855-电子普通发票.pdf', 'pdf', 'application/pdf', 63138, 'f9ab91d5d495d7d053e59c2df82158ab', 'local', 'storage\\uploads\\2062727546099822593.pdf', 'invoice', NULL, NULL, 'completed', 0, '2026-06-05 10:45:45', '2026-06-05 10:45:45');
INSERT INTO `file_asset` VALUES (2062728689785831425, '2062728689785831425.pdf', '机票预订出行订单尾号06809855-电子普通发票.pdf', 'pdf', 'application/pdf', 63138, 'f9ab91d5d495d7d053e59c2df82158ab', 'local', 'storage\\uploads\\2062728689785831425.pdf', 'invoice', NULL, NULL, 'completed', 0, '2026-06-05 10:50:17', '2026-06-05 10:50:18');
INSERT INTO `file_asset` VALUES (2062728819721175042, '2062728819721175042.pdf', '机票预订出行订单尾号06809855-电子普通发票.pdf', 'pdf', 'application/pdf', 63138, 'f9ab91d5d495d7d053e59c2df82158ab', 'local', 'storage\\uploads\\2062728819721175042.pdf', 'invoice', NULL, NULL, 'completed', 0, '2026-06-05 10:50:48', '2026-06-05 10:50:49');
INSERT INTO `file_asset` VALUES (2062729349029756929, '2062729349029756929.pdf', '滴滴电子发票A.pdf', 'pdf', 'application/pdf', 76610, 'fdf5aa3c70905ac7c44c6977ced45a6b', 'local', 'storage\\uploads\\2062729349029756929.pdf', 'invoice', NULL, NULL, 'completed', 0, '2026-06-05 10:52:55', '2026-06-05 10:52:55');
INSERT INTO `file_asset` VALUES (2062732332920520706, '2062732332920520706.pdf', '机票预订出行订单尾号06809855-电子普通发票.pdf', 'pdf', 'application/pdf', 63138, 'f9ab91d5d495d7d053e59c2df82158ab', 'local', 'storage\\uploads\\2062732332920520706.pdf', 'invoice', NULL, NULL, 'completed', 0, '2026-06-05 11:04:46', '2026-06-05 11:04:46');
INSERT INTO `file_asset` VALUES (2062732465506664449, '2062732465506664449.pdf', '滴滴电子发票A.pdf', 'pdf', 'application/pdf', 76610, 'fdf5aa3c70905ac7c44c6977ced45a6b', 'local', 'storage\\uploads\\2062732465506664449.pdf', 'invoice', NULL, NULL, 'completed', 0, '2026-06-05 11:05:18', '2026-06-05 11:05:18');

-- ----------------------------
-- Table structure for integration_connector
-- ----------------------------
DROP TABLE IF EXISTS `integration_connector`;
CREATE TABLE `integration_connector`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `connector_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '连接器编码',
  `connector_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '连接器名称',
  `connector_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '连接器类型',
  `auth_config_json` json NULL COMMENT '认证配置JSON',
  `endpoint_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接口地址',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'enabled' COMMENT '状态',
  `deleted` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_integration_connector_code`(`connector_code`) USING BTREE,
  INDEX `idx_integration_connector_type`(`connector_type`, `status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '第三方集成连接器表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of integration_connector
-- ----------------------------
INSERT INTO `integration_connector` VALUES (2062427545800847361, 'ORG-DEMO', '????', 'department', NULL, 'demo', 'enabled', 0, '2026-06-04 14:53:39', '2026-06-04 14:53:39');
INSERT INTO `integration_connector` VALUES (2062437015255891970, 'CONN-UI', 'Webhook???', 'webhook', NULL, 'https://example.com/webhook', 'enabled', 0, '2026-06-04 15:31:17', '2026-06-04 15:31:17');

-- ----------------------------
-- Table structure for invoice_record
-- ----------------------------
DROP TABLE IF EXISTS `invoice_record`;
CREATE TABLE `invoice_record`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `invoice_no` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '发票号码',
  `invoice_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发票代码',
  `file_id` bigint(0) NULL DEFAULT NULL COMMENT '文件ID',
  `buyer_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '购买方名称',
  `buyer_tax_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '购买方税号',
  `seller_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '销售方名称',
  `seller_tax_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '销售方税号',
  `invoice_date` date NULL DEFAULT NULL COMMENT '开票日期',
  `amount` decimal(20, 2) NOT NULL COMMENT '不含税金额',
  `tax_amount` decimal(20, 2) NOT NULL COMMENT '税额',
  `total_amount` decimal(20, 2) NOT NULL COMMENT '价税合计金额',
  `tax_rate` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '税率',
  `verify_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '校验状态',
  `duplicate_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '重复发票标识',
  `owner_user_id` bigint(0) NULL DEFAULT NULL COMMENT '归属用户ID',
  `org_id` bigint(0) NULL DEFAULT NULL COMMENT '组织ID',
  `deleted` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_invoice_seller`(`seller_tax_no`, `invoice_date`) USING BTREE,
  INDEX `idx_invoice_status`(`verify_status`, `duplicate_flag`) USING BTREE,
  INDEX `fk_invoice_file`(`file_id`) USING BTREE,
  INDEX `idx_invoice_no_code`(`invoice_no`, `invoice_code`) USING BTREE,
  CONSTRAINT `fk_invoice_file` FOREIGN KEY (`file_id`) REFERENCES `file_asset` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '发票记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of invoice_record
-- ----------------------------
INSERT INTO `invoice_record` VALUES (2062409003508449282, 'FP-2062409003508449282', NULL, NULL, '示例购买方有限公司', '91440000MADEMO001X', '示例供应商有限公司', '91440000MADEMO002X', NULL, 8600.00, 1118.00, 9718.00, '13%', 'passed', 0, NULL, NULL, 0, '2026-06-04 13:39:58', '2026-06-04 13:39:58');
INSERT INTO `invoice_record` VALUES (2062409486419664897, 'FP-2062409486419664897', NULL, NULL, '示例购买方有限公司', '91440000MADEMO001X', '示例供应商有限公司', '91440000MADEMO002X', NULL, 8600.00, 1118.00, 9718.00, '13%', 'passed', 0, NULL, NULL, 0, '2026-06-04 13:41:53', '2026-06-04 13:41:53');
INSERT INTO `invoice_record` VALUES (2062436954383958018, 'FP-2062436954383958018', NULL, NULL, '示例购买方有限公司', '91440000MADEMO001X', '示例供应商有限公司', '91440000MADEMO002X', NULL, 8600.00, 1118.00, 9718.00, '13%', 'passed', 0, NULL, NULL, 0, '2026-06-04 15:31:02', '2026-06-04 15:31:02');
INSERT INTO `invoice_record` VALUES (2062450559544320005, 'FP-AI-20260604', '', 2062450559481405442, '示例购买方有限公司', '待确认', '示例销售方有限公司', '待确认', NULL, 20260604001.00, 0.00, 20260604001.00, '6%', 'pending_review', 0, NULL, NULL, 0, '2026-06-04 16:25:07', '2026-06-04 16:25:07');
INSERT INTO `invoice_record` VALUES (2062451125368442883, 'FP-AI-20260604', 'AI-2062451125074841601', 2062451125074841601, '示例购买方有限公司', '待确认', '示例销售方有限公司', '待确认', NULL, 20260604001.00, 0.00, 20260604001.00, '6%', 'pending_review', 0, NULL, NULL, 0, '2026-06-04 16:27:21', '2026-06-04 16:27:21');
INSERT INTO `invoice_record` VALUES (2062506470157635588, 'FP-AI-20260604', 'AI-2062506469956308993', 2062506469956308993, '示例购买方有限公司', '待确认', '示例销售方有限公司', '待确认', NULL, 8600.00, 0.00, 8600.00, '6%', 'passed', 0, NULL, NULL, 0, '2026-06-04 20:07:17', '2026-06-04 20:07:17');
INSERT INTO `invoice_record` VALUES (2062515434643542019, 'FP-AI-20260604', 'AI-2062515434014396417', 2062515434014396417, '示例购买方有限公司', '待确认', '示例销售方有限公司', '待确认', NULL, 9188.00, 0.00, 9188.00, '6%', 'passed', 0, NULL, NULL, 0, '2026-06-04 20:42:54', '2026-06-04 20:42:54');
INSERT INTO `invoice_record` VALUES (2062517708644175875, 'FP-AI-20260604', 'AI-2062517708015030274', 2062517708015030274, '示例购买方有限公司', '待确认', '示例销售方有限公司', '待确认', NULL, 1160.00, 0.00, 1160.00, '6%', 'passed', 0, NULL, NULL, 0, '2026-06-04 20:51:56', '2026-06-04 20:51:56');
INSERT INTO `invoice_record` VALUES (2062519645401804803, '26317000001494503286', '26317000001494503286', 2062725744763600898, '上海华程西南国际旅行社有限公司', '91310105134638405A', '浙江毅星科技有限公司成都分公司', '91510100MAE8RKCX8J', '2026-04-29', 1094.34, 65.66, 1160.00, '6', 'passed', 1, NULL, NULL, 0, '2026-06-04 20:59:38', '2026-06-05 10:38:36');
INSERT INTO `invoice_record` VALUES (2062726965603954693, '26317000001494503286', '26317000001494503286', 2062726964958031873, '上海华程西南国际旅行社有限公司', '91310105134638405A', '浙江毅星科技有限公司成都分公司', '91510100MAE8RKCX8J', '2026-04-29', 1094.34, 65.66, 1160.00, '6%', 'passed', 1, NULL, NULL, 0, '2026-06-05 10:43:27', '2026-06-05 10:43:27');
INSERT INTO `invoice_record` VALUES (2062726966396678148, '26317000001494503286', '26317000001494503286', 2062726966132436993, '上海华程西南国际旅行社有限公司', '91310105134638405A', '浙江毅星科技有限公司成都分公司', '91510100MAE8RKCX8J', '2026-04-29', 1094.34, 65.66, 1160.00, '6%', 'passed', 1, NULL, NULL, 0, '2026-06-05 10:43:27', '2026-06-05 10:43:27');
INSERT INTO `invoice_record` VALUES (2062727191697911813, '26317000001494503286', '26317000001494503286', 2062727191484002305, '上海华程西南国际旅行社有限公司', '91310105134638405A', '浙江毅星科技有限公司成都分公司', '91510100MAE8RKCX8J', '2026-04-29', 1094.34, 65.66, 1160.00, '6%', 'passed', 1, NULL, NULL, 0, '2026-06-05 10:44:21', '2026-06-05 10:44:21');
INSERT INTO `invoice_record` VALUES (2062727546305343492, '26317000001494503286', '26317000001494503286', 2062727546099822593, '上海华程西南国际旅行社有限公司', '91310105134638405A', '浙江毅星科技有限公司成都分公司', '91510100MAE8RKCX8J', '2026-04-29', 1094.34, 65.66, 1160.00, '6%', 'passed', 1, NULL, NULL, 0, '2026-06-05 10:45:45', '2026-06-05 10:45:45');
INSERT INTO `invoice_record` VALUES (2062728690662440964, '26317000001494503286', '26317000001494503286', 2062728689785831425, '上海华程西南国际旅行社有限公司', '91310105134638405A', '浙江毅星科技有限公司成都分公司', '91510100MAE8RKCX8J', '2026-04-29', 1094.34, 65.66, 1160.00, '6%', 'passed', 1, NULL, NULL, 0, '2026-06-05 10:50:18', '2026-06-05 10:50:18');
INSERT INTO `invoice_record` VALUES (2062728819989610499, '26317000001494503286', '26317000001494503286', 2062728819721175042, '上海华程西南国际旅行社有限公司', '91310105134638405A', '浙江毅星科技有限公司成都分公司', '91510100MAE8RKCX8J', '2026-04-29', 1094.34, 65.66, 1160.00, '6%', 'passed', 1, NULL, NULL, 0, '2026-06-05 10:50:49', '2026-06-05 10:50:49');
INSERT INTO `invoice_record` VALUES (2062729349314969604, '26317000001494790709', '26317000001494790709', 2062729349029756929, '人工确认购买方有限公司', '91310114MA1GW61J6U', 'æµæ±æ¯æç§ææéå¬å¸æé½åå¬å¸', '91510100MAE8RKCX8J', '2026-04-29', 7.65, 0.00, 7.65, '3%', 'passed', 1, NULL, NULL, 0, '2026-06-05 10:52:55', '2026-06-05 11:02:37');
INSERT INTO `invoice_record` VALUES (2062732333541277700, '26317000001494503286', '26317000001494503286', 2062732332920520706, '上海华程西南国际旅行社有限公司', '91310105134638405A', '浙江毅星科技有限公司成都分公司', '91510100MAE8RKCX8J', '2026-04-29', 1094.34, 65.66, 1160.00, '6%', 'passed', 1, NULL, NULL, 0, '2026-06-05 11:04:47', '2026-06-05 11:05:01');
INSERT INTO `invoice_record` VALUES (2062732465716379653, '26317000001494790709', '26317000001494790709', 2062732465506664449, '上海滴滴畅行科技有限公司', '91310114MA1GW61J6U', '浙江毅星科技有限公司成都分公司', '91510100MAE8RKCX8J', '2026-04-29', 7.65, 0.00, 7.65, '3%', 'passed', 1, NULL, NULL, 0, '2026-06-05 11:05:18', '2026-06-05 11:05:35');

-- ----------------------------
-- Table structure for kb_chunk
-- ----------------------------
DROP TABLE IF EXISTS `kb_chunk`;
CREATE TABLE `kb_chunk`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `document_id` bigint(0) NOT NULL COMMENT '文档ID',
  `chunk_index` int(0) NOT NULL COMMENT '切片序号',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '知识切片内容',
  `token_count` int(0) NOT NULL DEFAULT 0 COMMENT 'Token数量',
  `embedding_model` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '向量模型名称',
  `vector_ref` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '向量存储引用',
  `metadata_json` json NULL COMMENT '元数据JSON',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_kb_chunk_doc_index`(`document_id`, `chunk_index`) USING BTREE,
  INDEX `idx_kb_chunk_vector`(`vector_ref`) USING BTREE,
  CONSTRAINT `fk_kb_chunk_document` FOREIGN KEY (`document_id`) REFERENCES `kb_document` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识库切片表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of kb_chunk
-- ----------------------------

-- ----------------------------
-- Table structure for kb_document
-- ----------------------------
DROP TABLE IF EXISTS `kb_document`;
CREATE TABLE `kb_document`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `space_id` bigint(0) NOT NULL COMMENT '知识库空间ID',
  `file_id` bigint(0) NULL DEFAULT NULL COMMENT '文件ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `doc_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'file' COMMENT '文档类型',
  `source_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源URL',
  `version_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'v1' COMMENT '版本号',
  `indexing_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '索引状态',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'enabled' COMMENT '状态',
  `deleted` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_kb_document_space`(`space_id`) USING BTREE,
  INDEX `idx_kb_document_status`(`indexing_status`, `status`) USING BTREE,
  INDEX `fk_kb_document_file`(`file_id`) USING BTREE,
  CONSTRAINT `fk_kb_document_file` FOREIGN KEY (`file_id`) REFERENCES `file_asset` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_kb_document_space` FOREIGN KEY (`space_id`) REFERENCES `kb_space` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识库文档表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of kb_document
-- ----------------------------

-- ----------------------------
-- Table structure for kb_space
-- ----------------------------
DROP TABLE IF EXISTS `kb_space`;
CREATE TABLE `kb_space`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `space_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '知识库空间编码',
  `space_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '知识库空间名称',
  `permission_scope` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'company' COMMENT '权限范围',
  `owner_org_id` bigint(0) NULL DEFAULT NULL COMMENT '归属组织ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'enabled' COMMENT '状态',
  `deleted` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_kb_space_code`(`space_code`) USING BTREE,
  INDEX `idx_kb_space_scope`(`permission_scope`, `status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识库空间表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of kb_space
-- ----------------------------

-- ----------------------------
-- Table structure for message_notification
-- ----------------------------
DROP TABLE IF EXISTS `message_notification`;
CREATE TABLE `message_notification`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `channel` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息渠道',
  `receiver_user_id` bigint(0) NULL DEFAULT NULL COMMENT '接收用户ID',
  `receiver_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接收地址',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `business_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务类型',
  `business_id` bigint(0) NULL DEFAULT NULL COMMENT '业务ID',
  `send_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '发送状态',
  `retry_count` int(0) NOT NULL DEFAULT 0 COMMENT '重试次数',
  `sent_at` datetime(0) NULL DEFAULT NULL COMMENT '发送时间',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_message_receiver`(`receiver_user_id`, `send_status`) USING BTREE,
  INDEX `idx_message_business`(`business_type`, `business_id`) USING BTREE,
  INDEX `idx_message_status`(`send_status`, `created_at`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of message_notification
-- ----------------------------
INSERT INTO `message_notification` VALUES (2062409487120113666, 'system', NULL, NULL, '????', '?????????', NULL, NULL, 'pending', 0, NULL, '2026-06-04 13:41:54', '2026-06-04 13:41:54');
INSERT INTO `message_notification` VALUES (2062436997832753153, 'system', NULL, NULL, '????', '????????????', NULL, NULL, 'pending', 0, NULL, '2026-06-04 15:31:13', '2026-06-04 15:31:13');

-- ----------------------------
-- Table structure for reconciliation_batch
-- ----------------------------
DROP TABLE IF EXISTS `reconciliation_batch`;
CREATE TABLE `reconciliation_batch`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `batch_no` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '批次编号',
  `supplier_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '供应商名称',
  `period_start` date NOT NULL COMMENT '账期开始日期',
  `period_end` date NOT NULL COMMENT '账期结束日期',
  `expected_amount` decimal(20, 2) NOT NULL COMMENT '应有金额',
  `actual_amount` decimal(20, 2) NOT NULL COMMENT '实际金额',
  `diff_amount` decimal(20, 2) NOT NULL COMMENT '差异金额',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '状态',
  `owner_user_id` bigint(0) NULL DEFAULT NULL COMMENT '归属用户ID',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_reconciliation_batch_no`(`batch_no`) USING BTREE,
  INDEX `idx_reconciliation_batch_period`(`period_start`, `period_end`) USING BTREE,
  INDEX `idx_reconciliation_batch_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '对账批次表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of reconciliation_batch
-- ----------------------------
INSERT INTO `reconciliation_batch` VALUES (2062427545586937858, 'ORG-DEMO', '????', '2026-06-01', '2026-06-04', 100.00, 100.00, 0.00, 'pending', NULL, '2026-06-04 14:53:39', '2026-06-04 14:53:39');

-- ----------------------------
-- Table structure for reconciliation_item
-- ----------------------------
DROP TABLE IF EXISTS `reconciliation_item`;
CREATE TABLE `reconciliation_item`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `batch_id` bigint(0) NOT NULL COMMENT '批次ID',
  `invoice_id` bigint(0) NULL DEFAULT NULL COMMENT '发票ID',
  `item_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '明细类型',
  `source_no` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源单号',
  `expected_amount` decimal(20, 2) NOT NULL COMMENT '应有金额',
  `actual_amount` decimal(20, 2) NOT NULL COMMENT '实际金额',
  `diff_amount` decimal(20, 2) NOT NULL COMMENT '差异金额',
  `diff_reason` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '差异原因',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'open' COMMENT '状态',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_reconciliation_item_batch`(`batch_id`) USING BTREE,
  INDEX `idx_reconciliation_item_status`(`status`) USING BTREE,
  INDEX `fk_reconciliation_item_invoice`(`invoice_id`) USING BTREE,
  CONSTRAINT `fk_reconciliation_item_batch` FOREIGN KEY (`batch_id`) REFERENCES `reconciliation_batch` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_reconciliation_item_invoice` FOREIGN KEY (`invoice_id`) REFERENCES `invoice_record` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '对账明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of reconciliation_item
-- ----------------------------
INSERT INTO `reconciliation_item` VALUES (2062427545586937859, 2062427545586937858, NULL, 'invoice', 'ORG-DEMO', 100.00, 100.00, 0.00, NULL, 'matched', '2026-06-04 14:53:39', '2026-06-04 14:53:39');

-- ----------------------------
-- Table structure for report_record
-- ----------------------------
DROP TABLE IF EXISTS `report_record`;
CREATE TABLE `report_record`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `template_id` bigint(0) NULL DEFAULT NULL COMMENT '模板ID',
  `report_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '报表类型',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '摘要',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '报表正文',
  `source_json` json NULL COMMENT '数据来源JSON',
  `generated_by` bigint(0) NULL DEFAULT NULL COMMENT '生成人用户ID',
  `send_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'draft' COMMENT '发送状态',
  `generated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '生成时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_report_record_type`(`report_type`, `generated_at`) USING BTREE,
  INDEX `idx_report_record_status`(`send_status`) USING BTREE,
  INDEX `fk_report_record_template`(`template_id`) USING BTREE,
  CONSTRAINT `fk_report_record_template` FOREIGN KEY (`template_id`) REFERENCES `report_template` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '报表生成记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of report_record
-- ----------------------------
INSERT INTO `report_record` VALUES (2062409004066291714, NULL, 'daily', 'daily - today', '销售新增客户稳定，客服高优工单上升，财务存在对账异常。', '一、销售进展；二、合同风险；三、财务对账；四、客服质量；五、明日重点。', '[\"CRM\", \"合同台账\", \"财务系统\", \"客服系统\"]', NULL, 'draft', '2026-06-04 13:39:58', '2026-06-04 13:39:58');
INSERT INTO `report_record` VALUES (2062434321359290369, NULL, 'daily', 'daily - today', '销售新增客户稳定，客服高优工单上升，财务存在对账异常。', '一、销售进展；二、合同风险；三、财务对账；四、客服质量；五、明日重点。', '[\"CRM\", \"合同台账\", \"财务系统\", \"客服系统\"]', NULL, 'draft', '2026-06-04 15:20:35', '2026-06-04 15:20:35');
INSERT INTO `report_record` VALUES (2062436541958045698, NULL, 'daily', 'daily - today', '销售新增客户稳定，客服高优工单上升，财务存在对账异常。', '一、销售进展；二、合同风险；三、财务对账；四、客服质量；五、明日重点。', '[\"CRM\", \"合同台账\", \"财务系统\", \"客服系统\"]', NULL, 'draft', '2026-06-04 15:29:24', '2026-06-04 15:29:24');
INSERT INTO `report_record` VALUES (2062436980384448513, NULL, 'daily', 'daily - today', '销售新增客户稳定，客服高优工单上升，财务存在对账异常。', '一、销售进展；二、合同风险；三、财务对账；四、客服质量；五、明日重点。', '[\"CRM\", \"合同台账\", \"财务系统\", \"客服系统\"]', NULL, 'draft', '2026-06-04 15:31:09', '2026-06-04 15:31:09');

-- ----------------------------
-- Table structure for report_template
-- ----------------------------
DROP TABLE IF EXISTS `report_template`;
CREATE TABLE `report_template`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `template_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板编码',
  `template_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板名称',
  `report_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '报表类型',
  `template_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板内容',
  `audience` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标受众',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'enabled' COMMENT '状态',
  `deleted` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_report_template_code`(`template_code`) USING BTREE,
  INDEX `idx_report_template_type`(`report_type`, `status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '报表模板表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of report_template
-- ----------------------------

-- ----------------------------
-- Table structure for review_task
-- ----------------------------
DROP TABLE IF EXISTS `review_task`;
CREATE TABLE `review_task`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `task_no` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务编号',
  `scenario` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务场景',
  `business_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型',
  `business_id` bigint(0) NOT NULL COMMENT '业务ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `risk_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'medium' COMMENT '风险等级',
  `assignee_user_id` bigint(0) NULL DEFAULT NULL COMMENT '处理人用户ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '状态',
  `review_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '复核结果',
  `completed_at` datetime(0) NULL DEFAULT NULL COMMENT '完成时间',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_review_task_no`(`task_no`) USING BTREE,
  INDEX `idx_review_task_business`(`business_type`, `business_id`) USING BTREE,
  INDEX `idx_review_task_assignee`(`assignee_user_id`, `status`) USING BTREE,
  INDEX `idx_review_task_risk`(`risk_level`, `status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '人工复核任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of review_task
-- ----------------------------
INSERT INTO `review_task` VALUES (2062409485442392067, 'RV-2062409485442392066', 'CONTRACT', 'contract_record', 2062409485115236354, '合同付款周期需复核', 'medium', NULL, 'pending', NULL, NULL, '2026-06-04 13:41:53', '2026-06-04 13:41:53');
INSERT INTO `review_task` VALUES (2062436551860797443, 'RV-2062436551860797442', 'CONTRACT', 'contract_record', 2062436551797882882, '合同付款周期需复核', 'medium', NULL, 'pending', NULL, NULL, '2026-06-04 15:29:26', '2026-06-04 15:29:26');
INSERT INTO `review_task` VALUES (2062436945693360133, 'RV-2062436945693360132', 'CONTRACT', 'contract_record', 2062436945693360130, '合同付款周期需复核', 'medium', NULL, 'pending', NULL, NULL, '2026-06-04 15:31:00', '2026-06-04 15:31:00');
INSERT INTO `review_task` VALUES (2062450456397996036, 'RV-2062450456397996035', 'contract', 'contract', 2062450456330887171, 'AI文件解析需要人工复核', 'medium', NULL, 'pending', NULL, NULL, '2026-06-04 16:24:41', '2026-06-04 16:24:41');
INSERT INTO `review_task` VALUES (2062450559611428867, 'RV-2062450559611428866', 'invoice', 'invoice', 2062450559544320005, 'AI文件解析需要人工复核', 'medium', NULL, 'pending', NULL, NULL, '2026-06-04 16:25:06', '2026-06-04 16:25:06');
INSERT INTO `review_task` VALUES (2062451125368442885, 'RV-2062451125368442884', 'invoice', 'invoice', 2062451125368442883, 'AI文件解析需要人工复核', 'medium', NULL, 'pending', NULL, NULL, '2026-06-04 16:27:21', '2026-06-04 16:27:21');
INSERT INTO `review_task` VALUES (2062508119714484227, 'RV-2062508119714484226', 'contract', 'contract', 2062508119647375365, 'AI文件解析需要人工复核', 'medium', NULL, 'pending', NULL, NULL, '2026-06-04 20:13:49', '2026-06-04 20:13:49');
INSERT INTO `review_task` VALUES (2062510051560538119, 'RV-2062510051560538118', 'contract', 'contract', 2062510051560538116, 'AI文件解析需要人工复核', 'medium', NULL, 'pending', NULL, NULL, '2026-06-04 20:21:30', '2026-06-04 20:21:30');
INSERT INTO `review_task` VALUES (2062511075310436356, 'RV-2062511075310436355', 'contract', 'contract', 2062511075243327492, 'AI文件解析需要人工复核', 'medium', NULL, 'pending', NULL, NULL, '2026-06-04 20:25:34', '2026-06-04 20:25:34');
INSERT INTO `review_task` VALUES (2062511248963010567, 'RV-2062511248963010566', 'contract', 'contract', 2062511248963010564, 'AI文件解析需要人工复核', 'medium', NULL, 'pending', NULL, NULL, '2026-06-04 20:26:16', '2026-06-04 20:26:16');
INSERT INTO `review_task` VALUES (2062711587913908227, 'RV-2062711587913908226', 'contract', 'contract', 2062711587846799365, 'AI文件解析需要人工复核', 'medium', NULL, 'pending', NULL, NULL, '2026-06-05 09:42:20', '2026-06-05 09:42:20');

-- ----------------------------
-- Table structure for sales_customer
-- ----------------------------
DROP TABLE IF EXISTS `sales_customer`;
CREATE TABLE `sales_customer`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `customer_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户名称',
  `industry` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '行业',
  `region` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地区',
  `contact_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人姓名',
  `contact_mobile` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人手机号',
  `owner_user_id` bigint(0) NULL DEFAULT NULL COMMENT '归属用户ID',
  `customer_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'normal' COMMENT '客户等级',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'active' COMMENT '状态',
  `deleted` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sales_customer_owner`(`owner_user_id`) USING BTREE,
  INDEX `idx_sales_customer_level`(`customer_level`, `status`) USING BTREE,
  INDEX `idx_sales_customer_name`(`customer_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '销售客户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sales_customer
-- ----------------------------

-- ----------------------------
-- Table structure for sales_followup_task
-- ----------------------------
DROP TABLE IF EXISTS `sales_followup_task`;
CREATE TABLE `sales_followup_task`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `opportunity_id` bigint(0) NULL DEFAULT NULL COMMENT '商机ID',
  `customer_id` bigint(0) NOT NULL COMMENT '客户ID',
  `task_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务标题',
  `next_action` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '下一步动作',
  `due_time` datetime(0) NOT NULL COMMENT '截止时间',
  `owner_user_id` bigint(0) NULL DEFAULT NULL COMMENT '归属用户ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '状态',
  `source_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ai' COMMENT '来源类型',
  `source_ref_id` bigint(0) NULL DEFAULT NULL COMMENT '来源引用ID',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sales_followup_customer`(`customer_id`) USING BTREE,
  INDEX `idx_sales_followup_due`(`due_time`, `status`) USING BTREE,
  INDEX `idx_sales_followup_owner`(`owner_user_id`) USING BTREE,
  INDEX `fk_sales_followup_opportunity`(`opportunity_id`) USING BTREE,
  CONSTRAINT `fk_sales_followup_customer` FOREIGN KEY (`customer_id`) REFERENCES `sales_customer` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_sales_followup_opportunity` FOREIGN KEY (`opportunity_id`) REFERENCES `sales_opportunity` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '销售跟进任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sales_followup_task
-- ----------------------------

-- ----------------------------
-- Table structure for sales_opportunity
-- ----------------------------
DROP TABLE IF EXISTS `sales_opportunity`;
CREATE TABLE `sales_opportunity`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `customer_id` bigint(0) NOT NULL COMMENT '客户ID',
  `opportunity_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商机名称',
  `stage` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'lead' COMMENT '销售阶段',
  `expected_amount` decimal(20, 2) NOT NULL COMMENT '应有金额',
  `expected_close_date` date NULL DEFAULT NULL COMMENT '预计成交日期',
  `win_probability` decimal(5, 2) NOT NULL COMMENT '赢单概率',
  `owner_user_id` bigint(0) NULL DEFAULT NULL COMMENT '归属用户ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'open' COMMENT '状态',
  `deleted` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sales_opportunity_customer`(`customer_id`) USING BTREE,
  INDEX `idx_sales_opportunity_stage`(`stage`, `status`) USING BTREE,
  INDEX `idx_sales_opportunity_owner`(`owner_user_id`) USING BTREE,
  CONSTRAINT `fk_sales_opportunity_customer` FOREIGN KEY (`customer_id`) REFERENCES `sales_customer` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '销售商机表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sales_opportunity
-- ----------------------------

-- ----------------------------
-- Table structure for sys_org
-- ----------------------------
DROP TABLE IF EXISTS `sys_org`;
CREATE TABLE `sys_org`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `parent_id` bigint(0) NOT NULL DEFAULT 0 COMMENT '父级ID',
  `org_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组织编码',
  `org_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组织名称',
  `org_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'department' COMMENT '组织类型',
  `sort_order` int(0) NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'enabled' COMMENT '状态',
  `deleted` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_org_code`(`org_code`) USING BTREE,
  INDEX `idx_sys_org_parent`(`parent_id`) USING BTREE,
  INDEX `idx_sys_org_status`(`status`, `deleted`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '组织架构表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_org
-- ----------------------------
INSERT INTO `sys_org` VALUES (2062427544253149185, 0, 'ORG-DEMO', '????', 'department', 0, 'enabled', 0, '2026-06-04 14:53:39', '2026-06-04 14:53:39');
INSERT INTO `sys_org` VALUES (2062437006506573826, 0, 'ORG-UI', '??????', 'department', 0, 'enabled', 0, '2026-06-04 15:31:15', '2026-06-04 15:31:15');
INSERT INTO `sys_org` VALUES (2062445703186145282, 0, 'ROOT', '默认企业', 'company', 1, 'enabled', 0, '2026-06-04 16:05:49', '2026-06-04 16:05:49');

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `permission_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限编码',
  `permission_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限名称',
  `permission_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'api' COMMENT '权限类型',
  `resource_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '资源路径',
  `parent_id` bigint(0) NOT NULL DEFAULT 0 COMMENT '父级ID',
  `sort_order` int(0) NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'enabled' COMMENT '状态',
  `deleted` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_permission_code`(`permission_code`) USING BTREE,
  INDEX `idx_sys_permission_parent`(`parent_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
INSERT INTO `sys_permission` VALUES (900000000000000101, '*', 'All Permissions', 'api', '*', 0, 1, 'enabled', 0, '2026-06-05 11:28:50', '2026-06-05 11:28:50');
INSERT INTO `sys_permission` VALUES (900000000000000102, 'system:manage', 'System Permission Management', 'menu', '/api/system/**', 0, 2, 'enabled', 0, '2026-06-05 11:28:50', '2026-06-05 11:28:50');
INSERT INTO `sys_permission` VALUES (900000000000000103, 'file:view', 'File Management', 'menu', '/api/files/**', 0, 3, 'enabled', 0, '2026-06-05 11:30:59', '2026-06-05 11:30:59');
INSERT INTO `sys_permission` VALUES (900000000000000104, 'contract:view', 'Contract Management', 'menu', '/api/contracts/**', 0, 4, 'enabled', 0, '2026-06-05 11:30:59', '2026-06-05 11:30:59');
INSERT INTO `sys_permission` VALUES (900000000000000105, 'finance:view', 'Finance Management', 'menu', '/api/invoices/**', 0, 5, 'enabled', 0, '2026-06-05 11:30:59', '2026-06-05 11:30:59');
INSERT INTO `sys_permission` VALUES (900000000000000106, 'ticket:view', 'Ticket Management', 'menu', '/api/tickets/**', 0, 6, 'enabled', 0, '2026-06-05 11:30:59', '2026-06-05 11:30:59');
INSERT INTO `sys_permission` VALUES (900000000000000107, 'sales:view', 'Sales Management', 'menu', '/api/sales/**', 0, 7, 'enabled', 0, '2026-06-05 11:30:59', '2026-06-05 11:30:59');
INSERT INTO `sys_permission` VALUES (900000000000000108, 'kb:view', 'Knowledge Base', 'menu', '/api/kb/**', 0, 8, 'enabled', 0, '2026-06-05 11:30:59', '2026-06-05 11:30:59');
INSERT INTO `sys_permission` VALUES (900000000000000109, 'report:view', 'Report Management', 'menu', '/api/reports/**', 0, 9, 'enabled', 0, '2026-06-05 11:30:59', '2026-06-05 11:30:59');
INSERT INTO `sys_permission` VALUES (900000000000000110, 'workflow:view', 'Workflow Management', 'menu', '/api/workflows/**', 0, 10, 'enabled', 0, '2026-06-05 11:30:59', '2026-06-05 11:30:59');
INSERT INTO `sys_permission` VALUES (900000000000000111, 'review:view', 'Review Management', 'menu', '/api/review/**', 0, 11, 'enabled', 0, '2026-06-05 11:30:59', '2026-06-05 11:30:59');
INSERT INTO `sys_permission` VALUES (900000000000000112, 'notification:view', 'Notification Management', 'menu', '/api/notifications/**', 0, 12, 'enabled', 0, '2026-06-05 11:30:59', '2026-06-05 11:30:59');
INSERT INTO `sys_permission` VALUES (900000000000000113, 'integration:view', 'Integration Management', 'menu', '/api/integrations/**', 0, 13, 'enabled', 0, '2026-06-05 11:30:59', '2026-06-05 11:30:59');
INSERT INTO `sys_permission` VALUES (900000000000000114, 'ai:manage', 'AI Governance', 'menu', '/api/ai-governance/**', 0, 14, 'enabled', 0, '2026-06-05 11:30:59', '2026-06-05 11:30:59');
INSERT INTO `sys_permission` VALUES (900000000000000115, 'audit:view', 'Audit Management', 'menu', '/api/audit/**', 0, 15, 'enabled', 0, '2026-06-05 11:30:59', '2026-06-05 11:30:59');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `role_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码',
  `role_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `data_scope` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'self' COMMENT '数据权限范围',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'enabled' COMMENT '状态',
  `deleted` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_role_code`(`role_code`) USING BTREE,
  INDEX `idx_sys_role_status`(`status`, `deleted`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (900000000000000001, 'SUPER_ADMIN', 'Super Administrator', 'all', 'enabled', 0, '2026-06-05 11:28:50', '2026-06-05 11:28:50');
INSERT INTO `sys_role` VALUES (2062739264532234241, 'FILE_VIEWER', 'File Viewer', 'self', 'enabled', 0, '2026-06-05 11:32:19', '2026-06-05 11:32:19');

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `role_id` bigint(0) NOT NULL COMMENT '角色ID',
  `permission_id` bigint(0) NOT NULL COMMENT '权限ID',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_role_permission`(`role_id`, `permission_id`) USING BTREE,
  INDEX `idx_sys_role_permission_permission`(`permission_id`) USING BTREE,
  CONSTRAINT `fk_sys_role_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `sys_permission` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_sys_role_permission_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------
INSERT INTO `sys_role_permission` VALUES (900000000000000201, 900000000000000001, 900000000000000101, '2026-06-05 11:30:59');
INSERT INTO `sys_role_permission` VALUES (900000000000000202, 900000000000000001, 900000000000000102, '2026-06-05 11:30:59');
INSERT INTO `sys_role_permission` VALUES (2062739265329152001, 2062739264532234241, 900000000000000103, '2026-06-05 11:32:19');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `org_id` bigint(0) NOT NULL COMMENT '组织ID',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录用户名',
  `real_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '真实姓名',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码哈希',
  `mobile` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像地址',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'enabled' COMMENT '状态',
  `last_login_at` datetime(0) NULL DEFAULT NULL COMMENT '最后登录时间',
  `deleted` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_user_username`(`username`) USING BTREE,
  INDEX `idx_sys_user_org`(`org_id`) USING BTREE,
  INDEX `idx_sys_user_status`(`status`, `deleted`) USING BTREE,
  CONSTRAINT `fk_sys_user_org` FOREIGN KEY (`org_id`) REFERENCES `sys_org` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (2062445703563632641, 2062445703186145282, 'admin', '系统管理员', '$2a$10$.2klhg1NXTmyGrfStG7IVumq8NnWzGZ.eiPfSvVxkf25wMHHamY0m', '13800000000', 'admin@qyglai.local', NULL, 'enabled', '2026-06-05 11:32:19', 0, '2026-06-04 16:05:49', '2026-06-04 16:05:49');
INSERT INTO `sys_user` VALUES (2062739265132019714, 2062427544253149185, 'fileviewer', '文件查看员', '$2a$10$CDaSpcrdk2e9T597OaLQ3.HbOCNeEFJ1HLZkOm516u14uYHI0WPAy', NULL, NULL, NULL, 'enabled', '2026-06-05 11:34:42', 0, '2026-06-05 11:32:19', '2026-06-05 11:32:19');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `role_id` bigint(0) NOT NULL COMMENT '角色ID',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_user_role`(`user_id`, `role_id`) USING BTREE,
  INDEX `idx_sys_user_role_role`(`role_id`) USING BTREE,
  CONSTRAINT `fk_sys_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_sys_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (900000000000000301, 2062445703563632641, 900000000000000001, '2026-06-05 11:30:59');
INSERT INTO `sys_user_role` VALUES (2062739265442398210, 2062739265132019714, 2062739264532234241, '2026-06-05 11:32:19');

-- ----------------------------
-- Table structure for ticket
-- ----------------------------
DROP TABLE IF EXISTS `ticket`;
CREATE TABLE `ticket`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `ticket_no` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工单编号',
  `customer_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户名称',
  `source_channel` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'manual' COMMENT '来源渠道',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '工单内容',
  `category` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类',
  `priority` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'P3' COMMENT '优先级',
  `sentiment` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '情绪倾向',
  `sla_deadline` datetime(0) NULL DEFAULT NULL COMMENT 'SLA截止时间',
  `assignee_user_id` bigint(0) NULL DEFAULT NULL COMMENT '处理人用户ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'open' COMMENT '状态',
  `confidence` decimal(5, 4) NOT NULL COMMENT '置信度',
  `deleted` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ticket_no`(`ticket_no`) USING BTREE,
  INDEX `idx_ticket_category`(`category`) USING BTREE,
  INDEX `idx_ticket_priority_status`(`priority`, `status`) USING BTREE,
  INDEX `idx_ticket_sla`(`sla_deadline`, `status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '客服工单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ticket
-- ----------------------------
INSERT INTO `ticket` VALUES (2062409003856576513, 'TK-2062409003856576513', '示例客户', 'manual', '客户咨询交付进度', '????', '交付进度咨询', 'P2', 'neutral', NULL, NULL, 'open', 0.8800, 0, '2026-06-04 13:39:58', '2026-06-04 13:39:58');
INSERT INTO `ticket` VALUES (2062409486604214274, 'TK-2062409486604214274', '示例客户', 'manual', '客户咨询交付进度', '????', '交付进度咨询', 'P2', 'neutral', NULL, NULL, 'open', 0.8800, 0, '2026-06-04 13:41:53', '2026-06-04 13:41:53');
INSERT INTO `ticket` VALUES (2062436963112304641, 'TK-2062436963112304641', '示例客户', 'manual', '客户咨询交付进度', '????????????', '交付进度咨询', 'P2', 'neutral', NULL, NULL, 'open', 0.8800, 0, '2026-06-04 15:31:04', '2026-06-04 15:31:04');

-- ----------------------------
-- Table structure for ticket_reply_suggestion
-- ----------------------------
DROP TABLE IF EXISTS `ticket_reply_suggestion`;
CREATE TABLE `ticket_reply_suggestion`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `ticket_id` bigint(0) NOT NULL COMMENT '工单ID',
  `suggestion_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '建议回复内容',
  `citation_json` json NULL COMMENT '引用来源JSON',
  `confidence` decimal(5, 4) NOT NULL COMMENT '置信度',
  `accepted_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '是否采纳标识',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ticket_reply_ticket`(`ticket_id`) USING BTREE,
  CONSTRAINT `fk_ticket_reply_ticket` FOREIGN KEY (`ticket_id`) REFERENCES `ticket` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工单AI回复建议表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ticket_reply_suggestion
-- ----------------------------
INSERT INTO `ticket_reply_suggestion` VALUES (2062409003856576514, 2062409003856576513, '建议先同步当前交付节点，并承诺下一次反馈时间。', '[\"历史工单FAQ\", \"交付SLA说明\"]', 0.8600, 0, '2026-06-04 13:39:58');
INSERT INTO `ticket_reply_suggestion` VALUES (2062409486604214275, 2062409486604214274, '建议先同步当前交付节点，并承诺下一次反馈时间。', '[\"历史工单FAQ\", \"交付SLA说明\"]', 0.8600, 0, '2026-06-04 13:41:53');
INSERT INTO `ticket_reply_suggestion` VALUES (2062436963112304642, 2062436963112304641, '建议先同步当前交付节点，并承诺下一次反馈时间。', '[\"历史工单FAQ\", \"交付SLA说明\"]', 0.8600, 0, '2026-06-04 15:31:04');

-- ----------------------------
-- Table structure for webhook_event
-- ----------------------------
DROP TABLE IF EXISTS `webhook_event`;
CREATE TABLE `webhook_event`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `connector_id` bigint(0) NULL DEFAULT NULL COMMENT '连接器ID',
  `event_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '事件类型',
  `event_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件键',
  `payload_json` json NOT NULL COMMENT '事件载荷JSON',
  `process_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '处理状态',
  `error_message` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `received_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '接收时间',
  `processed_at` datetime(0) NULL DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_webhook_event_connector`(`connector_id`) USING BTREE,
  INDEX `idx_webhook_event_status`(`process_status`, `received_at`) USING BTREE,
  INDEX `idx_webhook_event_key`(`event_key`) USING BTREE,
  CONSTRAINT `fk_webhook_event_connector` FOREIGN KEY (`connector_id`) REFERENCES `integration_connector` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Webhook事件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of webhook_event
-- ----------------------------
INSERT INTO `webhook_event` VALUES (2062427546148974593, NULL, 'demo.event', 'demo.event-1780556019708', '{\"hello\": \"world\"}', 'pending', NULL, '2026-06-04 14:53:40', NULL);
INSERT INTO `webhook_event` VALUES (2062437224849457153, NULL, 'ticket.created', 'ticket.created-1780558327291', '{\"payload\": {\"source\": \"frontend\"}}', 'pending', NULL, '2026-06-04 15:32:07', NULL);

-- ----------------------------
-- Table structure for workflow_definition
-- ----------------------------
DROP TABLE IF EXISTS `workflow_definition`;
CREATE TABLE `workflow_definition`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `workflow_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '流程编码',
  `workflow_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '流程名称',
  `scenario` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务场景',
  `version_no` int(0) NOT NULL DEFAULT 1 COMMENT '版本号',
  `definition_json` json NOT NULL COMMENT '流程定义JSON',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'enabled' COMMENT '状态',
  `deleted` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0未删除，1已删除',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_workflow_definition_code_version`(`workflow_code`, `version_no`) USING BTREE,
  INDEX `idx_workflow_definition_scenario`(`scenario`, `status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '流程定义表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of workflow_definition
-- ----------------------------
INSERT INTO `workflow_definition` VALUES (2062409485861822466, 'contract_review', 'contract_review流程', 'contract_review', 1, '{\"nodes\": [\"AI_CHECK\", \"MANUAL_REVIEW\", \"DONE\"]}', 'enabled', 0, '2026-06-04 13:41:53', '2026-06-04 13:41:53');
INSERT INTO `workflow_definition` VALUES (2062409486855872513, 'demo_flow', 'demo_flow流程', 'demo_flow', 1, '{\"nodes\": [\"AI_CHECK\", \"MANUAL_REVIEW\", \"DONE\"]}', 'enabled', 0, '2026-06-04 13:41:54', '2026-06-04 13:41:54');
INSERT INTO `workflow_definition` VALUES (2062434359372267521, 'ui_demo_flow', 'ui_demo_flow流程', 'ui_demo_flow', 1, '{\"nodes\": [\"AI_CHECK\", \"MANUAL_REVIEW\", \"DONE\"]}', 'enabled', 0, '2026-06-04 15:20:44', '2026-06-04 15:20:44');

-- ----------------------------
-- Table structure for workflow_instance
-- ----------------------------
DROP TABLE IF EXISTS `workflow_instance`;
CREATE TABLE `workflow_instance`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `definition_id` bigint(0) NOT NULL COMMENT '流程定义ID',
  `business_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型',
  `business_id` bigint(0) NULL DEFAULT NULL COMMENT '业务ID',
  `initiator_user_id` bigint(0) NULL DEFAULT NULL COMMENT '发起人用户ID',
  `current_node` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '当前节点',
  `variables_json` json NULL COMMENT '流程变量JSON',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'running' COMMENT '状态',
  `started_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '开始时间',
  `ended_at` datetime(0) NULL DEFAULT NULL COMMENT '结束时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_workflow_instance_definition`(`definition_id`) USING BTREE,
  INDEX `idx_workflow_instance_business`(`business_type`, `business_id`) USING BTREE,
  INDEX `idx_workflow_instance_status`(`status`, `started_at`) USING BTREE,
  CONSTRAINT `fk_workflow_instance_definition` FOREIGN KEY (`definition_id`) REFERENCES `workflow_definition` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '流程实例表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of workflow_instance
-- ----------------------------
INSERT INTO `workflow_instance` VALUES (2062409485861822467, 2062409485861822466, 'contract_review', NULL, NULL, 'AI_CHECK', '{\"contractId\": 2062409485115236354}', 'running', '2026-06-04 13:41:53', NULL, '2026-06-04 13:41:53');
INSERT INTO `workflow_instance` VALUES (2062409486855872514, 2062409486855872513, 'demo_flow', 1, NULL, 'AI_CHECK', '{\"businessId\": 1}', 'running', '2026-06-04 13:41:54', NULL, '2026-06-04 13:41:54');
INSERT INTO `workflow_instance` VALUES (2062434359372267522, 2062434359372267521, 'ui_demo_flow', 1, NULL, 'AI_CHECK', '{\"businessId\": 1}', 'running', '2026-06-04 15:20:44', NULL, '2026-06-04 15:20:44');
INSERT INTO `workflow_instance` VALUES (2062436551860797444, 2062409485861822466, 'contract_review', NULL, NULL, 'AI_CHECK', '{\"contractId\": 2062436551797882882}', 'running', '2026-06-04 15:29:26', NULL, '2026-06-04 15:29:26');
INSERT INTO `workflow_instance` VALUES (2062436945693360134, 2062409485861822466, 'contract_review', NULL, NULL, 'AI_CHECK', '{\"contractId\": 2062436945693360130}', 'running', '2026-06-04 15:31:00', NULL, '2026-06-04 15:31:00');
INSERT INTO `workflow_instance` VALUES (2062436989087629313, 2062434359372267521, 'ui_demo_flow', 1, NULL, 'AI_CHECK', '{\"businessId\": 1}', 'running', '2026-06-04 15:31:11', NULL, '2026-06-04 15:31:11');

-- ----------------------------
-- Table structure for workflow_task
-- ----------------------------
DROP TABLE IF EXISTS `workflow_task`;
CREATE TABLE `workflow_task`  (
  `id` bigint(0) NOT NULL COMMENT '主键ID',
  `instance_id` bigint(0) NOT NULL COMMENT '流程实例ID',
  `node_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '节点编码',
  `node_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '节点名称',
  `assignee_user_id` bigint(0) NULL DEFAULT NULL COMMENT '处理人用户ID',
  `task_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'manual' COMMENT '任务类型',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '状态',
  `due_time` datetime(0) NULL DEFAULT NULL COMMENT '截止时间',
  `completed_at` datetime(0) NULL DEFAULT NULL COMMENT '完成时间',
  `result_json` json NULL COMMENT '处理结果JSON',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_workflow_task_instance`(`instance_id`) USING BTREE,
  INDEX `idx_workflow_task_assignee`(`assignee_user_id`, `status`) USING BTREE,
  INDEX `idx_workflow_task_due`(`due_time`, `status`) USING BTREE,
  CONSTRAINT `fk_workflow_task_instance` FOREIGN KEY (`instance_id`) REFERENCES `workflow_instance` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '流程任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of workflow_task
-- ----------------------------
INSERT INTO `workflow_task` VALUES (2062409485996040193, 2062409485861822467, 'AI_CHECK', 'AI识别与规则校验', NULL, 'auto', 'pending', NULL, NULL, NULL, '2026-06-04 13:41:53', '2026-06-04 13:41:53');
INSERT INTO `workflow_task` VALUES (2062409486922981378, 2062409486855872514, 'AI_CHECK', 'AI识别与规则校验', NULL, 'auto', 'pending', NULL, NULL, NULL, '2026-06-04 13:41:54', '2026-06-04 13:41:54');
INSERT INTO `workflow_task` VALUES (2062434359372267523, 2062434359372267522, 'AI_CHECK', 'AI识别与规则校验', NULL, 'auto', 'pending', NULL, NULL, NULL, '2026-06-04 15:20:44', '2026-06-04 15:20:44');
INSERT INTO `workflow_task` VALUES (2062436551927906306, 2062436551860797444, 'AI_CHECK', 'AI识别与规则校验', NULL, 'auto', 'pending', NULL, NULL, NULL, '2026-06-04 15:29:26', '2026-06-04 15:29:26');
INSERT INTO `workflow_task` VALUES (2062436945693360135, 2062436945693360134, 'AI_CHECK', 'AI识别与规则校验', NULL, 'auto', 'pending', NULL, NULL, NULL, '2026-06-04 15:31:00', '2026-06-04 15:31:00');
INSERT INTO `workflow_task` VALUES (2062436989087629314, 2062436989087629313, 'AI_CHECK', 'AI识别与规则校验', NULL, 'auto', 'pending', NULL, NULL, NULL, '2026-06-04 15:31:11', '2026-06-04 15:31:11');

SET FOREIGN_KEY_CHECKS = 1;
