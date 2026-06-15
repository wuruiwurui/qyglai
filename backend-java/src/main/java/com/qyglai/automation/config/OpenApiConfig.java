package com.qyglai.automation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Swagger/OpenAPI 中文文档配置。
 *
 * <p>集中维护接口中文名称和说明，避免文档注解分散在业务控制器中。</p>
 */
@Configuration
public class OpenApiConfig {

    /**
     * 接口文档中文描述，键格式为“请求方法 接口路径”。
     */
    private static final Map<String, String> ENDPOINT_SUMMARIES = createEndpointSummaries();

    /**
     * 模块路径与中文标签的映射。
     */
    private static final LinkedHashMap<String, String> MODULE_TAGS = createModuleTags();

    /**
     * 创建系统 OpenAPI 基础信息。
     *
     * @return OpenAPI 基础信息
     */
    @Bean
    public OpenAPI automationOpenApi() {
        return new OpenAPI().info(new Info()
                .title("企业流程智能自动化平台接口文档")
                .description("面向合同、发票、知识库、工作流、销售、报表及 AI 治理的后端接口。")
                .version("1.0.0"));
    }

    /**
     * 为全部接口补充中文名称、中文说明和中文模块标签。
     *
     * @return OpenAPI 文档定制器
     */
    @Bean
    public OpenApiCustomizer chineseOpenApiCustomizer() {
        return openApi -> {
            List<Tag> tags = MODULE_TAGS.values().stream()
                    .distinct()
                    .map(name -> new Tag().name(name).description(name + "相关接口"))
                    .toList();
            openApi.setTags(tags);

            openApi.getPaths().forEach((path, pathItem) ->
                    pathItem.readOperationsMap().forEach((method, operation) -> {
                        String key = method.name() + " " + path;
                        String summary = ENDPOINT_SUMMARIES.getOrDefault(key, defaultSummary(method, path));
                        operation.setSummary(summary);
                        operation.setDescription(summary + "。");
                        operation.setTags(List.of(resolveTag(path)));
                    }));
        };
    }

    private static String resolveTag(String path) {
        return MODULE_TAGS.entrySet().stream()
                .filter(entry -> path.startsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("其他接口");
    }

    private static String defaultSummary(HttpMethod method, String path) {
        String action = switch (method) {
            case GET -> "查询";
            case POST -> "新增或执行";
            case PUT -> "更新";
            case DELETE -> "删除";
            case PATCH -> "局部更新";
            default -> "访问";
        };
        return action + "接口：" + path;
    }

    private static LinkedHashMap<String, String> createModuleTags() {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put("/api/ai-governance", "AI 治理");
        tags.put("/api/ai-tasks", "AI 业务任务");
        tags.put("/api/auth", "登录认证");
        tags.put("/api/boss-assistant", "老板智能问答");
        tags.put("/api/contracts", "合同管理");
        tags.put("/api/files", "文件处理");
        tags.put("/api/health", "系统健康");
        tags.put("/api/integrations", "系统集成");
        tags.put("/api/invoices", "发票管理");
        tags.put("/api/kb", "知识库");
        tags.put("/api/modules", "功能模块");
        tags.put("/api/notifications", "通知管理");
        tags.put("/api/reconciliation", "对账管理");
        tags.put("/api/reports", "报表管理");
        tags.put("/api/review", "人工复核");
        tags.put("/api/sales", "销售跟进");
        tags.put("/api/system", "系统管理");
        tags.put("/api/tickets", "客服工单");
        tags.put("/api/workflows", "审批工作流");
        tags.put("/api/audit", "审计日志");
        return tags;
    }

    private static Map<String, String> createEndpointSummaries() {
        Map<String, String> docs = new LinkedHashMap<>();

        add(docs, "GET", "/api/ai-governance/runtime-config", "查询 AI 运行配置");
        add(docs, "POST", "/api/ai-governance/runtime-config", "保存 AI 运行配置");
        add(docs, "GET", "/api/ai-governance/runtime-status", "查询 AI 运行状态");
        add(docs, "GET", "/api/ai-governance/embedding-config", "查询 Embedding 与向量库配置");
        add(docs, "POST", "/api/ai-governance/embedding-config", "保存 Embedding 与向量库配置");
        add(docs, "POST", "/api/ai-governance/embedding-config/test", "测试 Embedding 与向量库连接");
        add(docs, "GET", "/api/ai-governance/embedding-logs", "查询 Embedding 调用日志");
        add(docs, "GET", "/api/ai-governance/model-health", "查询模型健康状态");
        add(docs, "POST", "/api/ai-governance/model-health/check-all", "检查全部模型健康状态");
        add(docs, "POST", "/api/ai-governance/model-profiles/{id}/health-check", "检查指定模型健康状态");
        add(docs, "GET", "/api/ai-governance/model-profiles", "查询模型配置列表");
        add(docs, "POST", "/api/ai-governance/model-profiles", "保存模型配置");
        add(docs, "POST", "/api/ai-governance/model-profiles/{id}/switch", "切换当前使用模型");
        add(docs, "DELETE", "/api/ai-governance/model-profiles/{id}", "删除模型配置");
        add(docs, "GET", "/api/ai-governance/scenario-routes", "查询场景模型路由");
        add(docs, "POST", "/api/ai-governance/scenario-routes", "保存场景模型路由");
        add(docs, "DELETE", "/api/ai-governance/scenario-routes/{scenario}", "删除场景模型路由");
        add(docs, "GET", "/api/ai-governance/model-call-logs", "查询模型调用日志");
        add(docs, "GET", "/api/ai-governance/evaluation-dashboard", "查询 AI 效果评估看板");
        add(docs, "GET", "/api/ai-governance/providers", "查询模型提供商列表");
        add(docs, "POST", "/api/ai-governance/prompt-templates", "保存提示词模板");
        add(docs, "GET", "/api/ai-governance/prompt-templates", "查询提示词模板");
        add(docs, "GET", "/api/ai-governance/evaluation-samples", "查询 AI 评估样本");

        add(docs, "GET", "/api/audit/logs", "查询系统审计日志");
        add(docs, "POST", "/api/auth/login", "用户登录");
        add(docs, "GET", "/api/auth/me", "查询当前登录用户");
        add(docs, "POST", "/api/boss-assistant/ask", "向老板智能助手提问");
        add(docs, "POST", "/api/contracts/extract", "提取合同结构化信息");
        add(docs, "GET", "/api/contracts", "查询合同列表");

        add(docs, "GET", "/api/files", "查询文件列表");
        add(docs, "GET", "/api/files/{id}/detail", "查询文件处理详情");
        add(docs, "POST", "/api/files/upload", "上传业务文件");
        add(docs, "POST", "/api/files/parse-results", "保存文件解析结果");
        add(docs, "POST", "/api/files/ai-process", "使用 AI 处理文件");
        add(docs, "POST", "/api/files/parse-and-extract", "解析文件并提取业务数据");
        add(docs, "POST", "/api/files/{id}/confirm-fields", "确认或修正文件字段");
        add(docs, "GET", "/api/files/{id}/corrections", "查询字段修正历史");
        add(docs, "GET", "/api/health", "查询系统健康状态");

        add(docs, "POST", "/api/integrations/connectors", "保存外部系统连接器");
        add(docs, "GET", "/api/integrations/connectors", "查询外部系统连接器");
        add(docs, "POST", "/api/integrations/webhooks", "保存 Webhook 配置");
        add(docs, "GET", "/api/integrations/webhooks", "查询 Webhook 配置");
        add(docs, "POST", "/api/invoices/parse", "解析发票信息");
        add(docs, "GET", "/api/invoices", "查询发票列表");

        add(docs, "POST", "/api/ai-tasks/sales/followup-reminder", "生成销售跟进提醒");
        add(docs, "POST", "/api/ai-tasks/reconciliation/analyze", "执行智能对账分析");
        add(docs, "POST", "/api/ai-tasks/review/advice", "生成智能复核建议");
        add(docs, "POST", "/api/ai-tasks/prompts/evaluate", "评估提示词效果");

        add(docs, "POST", "/api/kb/query", "知识库智能问答");
        add(docs, "GET", "/api/kb/search", "搜索知识库内容");
        add(docs, "POST", "/api/kb/spaces", "创建知识空间");
        add(docs, "GET", "/api/kb/spaces", "查询知识空间");
        add(docs, "POST", "/api/kb/documents/text", "导入知识文本");
        add(docs, "POST", "/api/kb/documents/file", "导入知识文件");
        add(docs, "GET", "/api/kb/documents", "查询知识文档");
        add(docs, "GET", "/api/kb/vector-status", "查询知识库向量化状态");
        add(docs, "POST", "/api/kb/vectors/reindex", "重建知识库向量索引");

        add(docs, "GET", "/api/modules", "查询系统功能模块");
        add(docs, "POST", "/api/notifications", "创建业务通知");
        add(docs, "GET", "/api/notifications", "查询业务通知");
        add(docs, "POST", "/api/reconciliation/batches", "创建对账批次");
        add(docs, "GET", "/api/reconciliation/batches", "查询对账批次");
        add(docs, "GET", "/api/reconciliation/items", "查询对账明细");
        add(docs, "POST", "/api/reports/generate", "生成日报或周报");
        add(docs, "GET", "/api/reports", "查询报表列表");
        add(docs, "GET", "/api/review/tasks", "查询人工复核任务");
        add(docs, "PUT", "/api/review/tasks/{id}/complete", "完成人工复核任务");
        add(docs, "GET", "/api/sales/followups", "查询销售跟进任务");

        add(docs, "POST", "/api/system/orgs", "创建组织机构");
        add(docs, "GET", "/api/system/orgs", "查询组织机构");
        add(docs, "GET", "/api/system/users", "查询用户列表");
        add(docs, "GET", "/api/system/roles", "查询角色列表");
        add(docs, "GET", "/api/system/permissions", "查询权限列表");
        add(docs, "POST", "/api/system/orgs/manage", "保存组织机构");
        add(docs, "PUT", "/api/system/orgs/{id}", "更新组织机构");
        add(docs, "POST", "/api/system/users", "创建用户");
        add(docs, "PUT", "/api/system/users/{id}", "更新用户");
        add(docs, "POST", "/api/system/roles", "创建角色");
        add(docs, "PUT", "/api/system/roles/{id}", "更新角色");
        add(docs, "POST", "/api/system/permissions", "创建权限");
        add(docs, "PUT", "/api/system/permissions/{id}", "更新权限");
        add(docs, "GET", "/api/system/users/{id}/role-ids", "查询用户角色");
        add(docs, "PUT", "/api/system/users/{id}/role-ids", "分配用户角色");
        add(docs, "GET", "/api/system/roles/{id}/permission-ids", "查询角色权限");
        add(docs, "PUT", "/api/system/roles/{id}/permission-ids", "分配角色权限");

        add(docs, "POST", "/api/tickets/classify", "智能分类客服工单");
        add(docs, "GET", "/api/tickets", "查询客服工单");
        add(docs, "POST", "/api/workflows/start", "启动审批流程");
        add(docs, "GET", "/api/workflows/tasks", "查询待办审批任务");
        add(docs, "GET", "/api/workflows/definitions", "查询流程定义");
        add(docs, "POST", "/api/workflows/definitions", "保存流程定义");
        add(docs, "GET", "/api/workflows/instances", "查询流程实例");
        add(docs, "GET", "/api/workflows/instances/{instanceId}", "查询流程实例详情");
        add(docs, "POST", "/api/workflows/tasks/{taskId}/actions", "执行审批任务操作");
        return docs;
    }

    private static void add(Map<String, String> docs, String method, String path, String summary) {
        docs.put(method + " " + path, summary);
    }
}
