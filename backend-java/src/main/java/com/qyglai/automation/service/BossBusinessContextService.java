package com.qyglai.automation.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.qyglai.automation.dto.BossChatRequest;
import com.qyglai.automation.dto.BossChatResponse;
import com.qyglai.automation.dto.MetricCard;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 老板问答真实经营数据查询服务。
 *
 * <p>所有数量、金额和业务明细均由 Java 查询 MySQL 并计算，大模型只负责理解和总结。</p>
 */
@Service
public class BossBusinessContextService {

    private final JdbcTemplate jdbcTemplate;

    public BossBusinessContextService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 根据问题意图和时间范围构建真实经营上下文。
     *
     * @param request 老板问答请求
     * @return 可安全交给大模型总结的结构化经营数据
     */
    public Map<String, Object> buildContext(BossChatRequest request) {
        String intent = classifyIntent(request == null ? null : request.question());
        String timeRange = request == null || request.timeRange() == null ? "this_week" : request.timeRange();
        LocalDateTime start = rangeStart(timeRange);
        Map<String, Object> context = switch (intent) {
            case "sales" -> salesContext(start);
            case "finance" -> financeContext(start);
            case "customer_service" -> ticketContext(start);
            case "contract" -> contractContext(start);
            case "review" -> reviewContext(start);
            default -> overviewContext(start);
        };
        context.put("intent", intent);
        context.put("timeRange", timeRange);
        context.put("rangeStart", start.toString());
        context.put("generatedAt", LocalDateTime.now().toString());
        context.put("dataPolicy", "所有指标和明细均由Java查询MySQL生成；模型不得编造或修改数字。");
        return context;
    }

    /**
     * 在 AI 服务不可用时，直接使用真实数据库上下文返回结果。
     */
    public BossChatResponse fallbackResponse(Map<String, Object> context) {
        List<MetricCard> metrics = metricCards(context.get("metrics"));
        return new BossChatResponse(
                String.valueOf(context.getOrDefault("intent", "overview")),
                String.valueOf(context.getOrDefault("summary", "已查询数据库，但暂无可汇总的经营数据。")),
                metrics,
                stringList(context.get("actions")),
                stringList(context.get("sources"))
        );
    }

    private Map<String, Object> salesContext(LocalDateTime start) {
        long openOpportunities = count("SELECT COUNT(*) FROM sales_opportunity WHERE deleted=0 AND status='open'");
        BigDecimal pipeline = amount("SELECT COALESCE(SUM(expected_amount),0) FROM sales_opportunity WHERE deleted=0 AND status='open'");
        long pendingFollowups = count("SELECT COUNT(*) FROM sales_followup_task WHERE status='pending'");
        long overdueFollowups = count("SELECT COUNT(*) FROM sales_followup_task WHERE status='pending' AND due_time < NOW()");
        long newCustomers = countSince("SELECT COUNT(*) FROM sales_customer WHERE deleted=0 AND created_at >= ?", start);
        List<Map<String, Object>> details = jdbcTemplate.queryForList("""
                SELECT f.id, c.customer_name, o.opportunity_name, o.stage, o.expected_amount,
                       f.task_title, f.next_action, f.due_time
                FROM sales_followup_task f
                LEFT JOIN sales_customer c ON c.id=f.customer_id
                LEFT JOIN sales_opportunity o ON o.id=f.opportunity_id
                WHERE f.status='pending'
                ORDER BY (f.due_time < NOW()) DESC, f.due_time ASC
                LIMIT 10
                """);
        return context(
                "销售管道金额为" + money(pipeline) + "元，当前开放商机" + openOpportunities
                        + "个，待跟进任务" + pendingFollowups + "个，其中逾期" + overdueFollowups + "个。",
                List.of(metric("开放商机", openOpportunities, "normal"),
                        metric("销售管道金额", money(pipeline), "normal"),
                        metric("待跟进任务", pendingFollowups, pendingFollowups > 0 ? "warning" : "normal"),
                        metric("逾期跟进", overdueFollowups, overdueFollowups > 0 ? "danger" : "normal"),
                        metric("期间新增客户", newCustomers, "normal")),
                List.of("查看逾期跟进", "查看重点商机", "生成销售跟进清单"),
                List.of("sales_opportunity", "sales_followup_task", "sales_customer"),
                details
        );
    }

    private Map<String, Object> financeContext(LocalDateTime start) {
        long invoices = countSince("SELECT COUNT(*) FROM invoice_record WHERE deleted=0 AND created_at >= ?", start);
        BigDecimal invoiceTotal = amountSince("SELECT COALESCE(SUM(total_amount),0) FROM invoice_record WHERE deleted=0 AND created_at >= ?", start);
        long abnormalInvoices = countSince("""
                SELECT COUNT(*) FROM invoice_record
                WHERE deleted=0 AND created_at >= ? AND (duplicate_flag=1 OR verify_status NOT IN ('verified','approved','passed'))
                """, start);
        long reconciliationExceptions = count("""
                SELECT COUNT(*) FROM reconciliation_item
                WHERE status NOT IN ('matched','closed','resolved') OR diff_amount <> 0
                """);
        BigDecimal difference = amount("""
                SELECT COALESCE(SUM(ABS(diff_amount)),0) FROM reconciliation_item
                WHERE status NOT IN ('matched','closed','resolved') OR diff_amount <> 0
                """);
        List<Map<String, Object>> details = jdbcTemplate.queryForList("""
                SELECT id, batch_id, source_no, expected_amount, actual_amount, diff_amount, diff_reason, status
                FROM reconciliation_item
                WHERE status NOT IN ('matched','closed','resolved') OR diff_amount <> 0
                ORDER BY ABS(diff_amount) DESC, created_at DESC
                LIMIT 10
                """);
        return context(
                "期间新增发票" + invoices + "张，价税合计" + money(invoiceTotal) + "元；异常发票"
                        + abnormalInvoices + "张，对账异常" + reconciliationExceptions + "笔，差异金额" + money(difference) + "元。",
                List.of(metric("期间发票", invoices, "normal"),
                        metric("发票价税合计", money(invoiceTotal), "normal"),
                        metric("异常发票", abnormalInvoices, abnormalInvoices > 0 ? "warning" : "normal"),
                        metric("对账异常", reconciliationExceptions, reconciliationExceptions > 0 ? "danger" : "normal"),
                        metric("对账差异金额", money(difference), difference.signum() > 0 ? "danger" : "normal")),
                List.of("查看对账异常明细", "核验异常发票", "导出发票台账"),
                List.of("invoice_record", "reconciliation_item", "reconciliation_batch"),
                details
        );
    }

    private Map<String, Object> ticketContext(LocalDateTime start) {
        long periodTickets = countSince("SELECT COUNT(*) FROM ticket WHERE deleted=0 AND created_at >= ?", start);
        long openTickets = count("SELECT COUNT(*) FROM ticket WHERE deleted=0 AND status NOT IN ('closed','resolved','completed')");
        long highPriority = count("SELECT COUNT(*) FROM ticket WHERE deleted=0 AND priority IN ('P0','P1') AND status NOT IN ('closed','resolved','completed')");
        long overdue = count("""
                SELECT COUNT(*) FROM ticket
                WHERE deleted=0 AND sla_deadline < NOW() AND status NOT IN ('closed','resolved','completed')
                """);
        List<Map<String, Object>> details = jdbcTemplate.queryForList("""
                SELECT id, ticket_no, customer_name, title, category, priority, status, sla_deadline
                FROM ticket
                WHERE deleted=0 AND status NOT IN ('closed','resolved','completed')
                ORDER BY (sla_deadline < NOW()) DESC, priority ASC, created_at DESC
                LIMIT 10
                """);
        return context(
                "期间新增工单" + periodTickets + "个，当前待处理" + openTickets + "个，高优工单"
                        + highPriority + "个，SLA逾期" + overdue + "个。",
                List.of(metric("期间新增工单", periodTickets, "normal"),
                        metric("待处理工单", openTickets, openTickets > 0 ? "warning" : "normal"),
                        metric("高优工单", highPriority, highPriority > 0 ? "danger" : "normal"),
                        metric("SLA逾期", overdue, overdue > 0 ? "danger" : "normal")),
                List.of("查看高优工单", "催办SLA逾期工单", "生成客服处理清单"),
                List.of("ticket"),
                details
        );
    }

    private Map<String, Object> contractContext(LocalDateTime start) {
        long periodContracts = countSince("SELECT COUNT(*) FROM contract_record WHERE deleted=0 AND created_at >= ?", start);
        BigDecimal contractAmount = amountSince("SELECT COALESCE(SUM(amount),0) FROM contract_record WHERE deleted=0 AND created_at >= ?", start);
        long pendingReview = count("SELECT COUNT(*) FROM contract_record WHERE deleted=0 AND review_status NOT IN ('approved','passed','completed')");
        long highRisk = count("SELECT COUNT(*) FROM contract_record WHERE deleted=0 AND risk_level IN ('high','critical')");
        long openRisks = count("SELECT COUNT(*) FROM contract_risk_item WHERE status NOT IN ('closed','resolved')");
        List<Map<String, Object>> details = jdbcTemplate.queryForList("""
                SELECT id, contract_no, party_a, party_b, amount, risk_level, review_status, end_date
                FROM contract_record
                WHERE deleted=0 AND (risk_level IN ('high','critical') OR review_status NOT IN ('approved','passed','completed'))
                ORDER BY FIELD(risk_level,'critical','high','medium','low'), amount DESC
                LIMIT 10
                """);
        return context(
                "期间新增合同" + periodContracts + "份，金额合计" + money(contractAmount) + "元；待复核合同"
                        + pendingReview + "份，高风险合同" + highRisk + "份，未关闭风险项" + openRisks + "个。",
                List.of(metric("期间新增合同", periodContracts, "normal"),
                        metric("期间合同金额", money(contractAmount), "normal"),
                        metric("待复核合同", pendingReview, pendingReview > 0 ? "warning" : "normal"),
                        metric("高风险合同", highRisk, highRisk > 0 ? "danger" : "normal"),
                        metric("未关闭风险项", openRisks, openRisks > 0 ? "danger" : "normal")),
                List.of("查看高风险合同", "处理待复核合同", "查看未关闭风险项"),
                List.of("contract_record", "contract_risk_item"),
                details
        );
    }

    private Map<String, Object> reviewContext(LocalDateTime start) {
        long pendingReviews = count("SELECT COUNT(*) FROM review_task WHERE status NOT IN ('completed','approved','rejected','closed')");
        long highRisk = count("""
                SELECT COUNT(*) FROM review_task
                WHERE risk_level IN ('high','critical') AND status NOT IN ('completed','approved','rejected','closed')
                """);
        long workflowPending = count("SELECT COUNT(*) FROM workflow_task WHERE status IN ('pending','waiting','in_progress')");
        long workflowOverdue = count("""
                SELECT COUNT(*) FROM workflow_task
                WHERE due_time < NOW() AND status IN ('pending','waiting','in_progress')
                """);
        long completed = countSince("SELECT COUNT(*) FROM review_task WHERE completed_at >= ?", start);
        List<Map<String, Object>> details = jdbcTemplate.queryForList("""
                SELECT id, task_no, scenario, business_type, business_id, title, risk_level, status, created_at
                FROM review_task
                WHERE status NOT IN ('completed','approved','rejected','closed')
                ORDER BY FIELD(risk_level,'critical','high','medium','low'), created_at ASC
                LIMIT 10
                """);
        return context(
                "当前待复核" + pendingReviews + "项，其中高风险" + highRisk + "项；流程待办"
                        + workflowPending + "项，逾期" + workflowOverdue + "项，期间已完成复核" + completed + "项。",
                List.of(metric("待复核", pendingReviews, pendingReviews > 0 ? "warning" : "normal"),
                        metric("高风险复核", highRisk, highRisk > 0 ? "danger" : "normal"),
                        metric("流程待办", workflowPending, workflowPending > 0 ? "warning" : "normal"),
                        metric("逾期流程", workflowOverdue, workflowOverdue > 0 ? "danger" : "normal"),
                        metric("期间完成复核", completed, "normal")),
                List.of("查看高风险复核", "催办逾期流程", "查看复核任务"),
                List.of("review_task", "workflow_task"),
                details
        );
    }

    private Map<String, Object> overviewContext(LocalDateTime start) {
        Map<String, Object> sales = salesContext(start);
        Map<String, Object> finance = financeContext(start);
        Map<String, Object> tickets = ticketContext(start);
        Map<String, Object> contracts = contractContext(start);
        Map<String, Object> reviews = reviewContext(start);
        long overdueSales = count("SELECT COUNT(*) FROM sales_followup_task WHERE status='pending' AND due_time < NOW()");
        long reconciliationExceptions = count("SELECT COUNT(*) FROM reconciliation_item WHERE status NOT IN ('matched','closed','resolved') OR diff_amount <> 0");
        long highPriorityTickets = count("SELECT COUNT(*) FROM ticket WHERE deleted=0 AND priority IN ('P0','P1') AND status NOT IN ('closed','resolved','completed')");
        long highRiskContracts = count("SELECT COUNT(*) FROM contract_record WHERE deleted=0 AND risk_level IN ('high','critical')");
        long pendingReviews = count("SELECT COUNT(*) FROM review_task WHERE status NOT IN ('completed','approved','rejected','closed')");
        List<Map<String, Object>> metrics = new ArrayList<>();
        metrics.add(metric("逾期销售跟进", overdueSales, overdueSales > 0 ? "warning" : "normal"));
        metrics.add(metric("对账异常", reconciliationExceptions, reconciliationExceptions > 0 ? "danger" : "normal"));
        metrics.add(metric("高优待处理工单", highPriorityTickets, highPriorityTickets > 0 ? "danger" : "normal"));
        metrics.add(metric("高风险合同", highRiskContracts, highRiskContracts > 0 ? "danger" : "normal"));
        metrics.add(metric("待复核事项", pendingReviews, pendingReviews > 0 ? "warning" : "normal"));
        Map<String, Object> result = context(
                "经营总览基于销售、财务、客服、合同和审批数据库实时汇总。请优先关注状态为warning或danger的指标。",
                metrics,
                List.of("查看逾期销售跟进", "查看财务异常", "查看高风险合同", "处理审批待办"),
                List.of("sales_followup_task", "reconciliation_item", "ticket", "contract_record", "review_task"),
                List.of()
        );
        result.put("modules", Map.of("sales", sales, "finance", finance, "customerService", tickets, "contract", contracts, "review", reviews));
        return result;
    }

    private Map<String, Object> context(String summary, List<Map<String, Object>> metrics, List<String> actions,
                                        List<String> sources, List<Map<String, Object>> details) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("metrics", metrics);
        result.put("actions", actions);
        result.put("sources", sources);
        result.put("details", details);
        return result;
    }

    private Map<String, Object> metric(String name, Object value, String status) {
        return Map.of("name", name, "value", String.valueOf(value), "trend", "数据库实时值", "status", status);
    }

    private long count(String sql) {
        Number value = jdbcTemplate.queryForObject(sql, Number.class);
        return value == null ? 0 : value.longValue();
    }

    private long countSince(String sql, LocalDateTime start) {
        Number value = jdbcTemplate.queryForObject(sql, Number.class, Timestamp.valueOf(start));
        return value == null ? 0 : value.longValue();
    }

    private BigDecimal amount(String sql) {
        BigDecimal value = jdbcTemplate.queryForObject(sql, BigDecimal.class);
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal amountSince(String sql, LocalDateTime start) {
        BigDecimal value = jdbcTemplate.queryForObject(sql, BigDecimal.class, Timestamp.valueOf(start));
        return value == null ? BigDecimal.ZERO : value;
    }

    private String money(BigDecimal value) {
        return value == null ? "0.00" : value.stripTrailingZeros().toPlainString();
    }

    private LocalDateTime rangeStart(String timeRange) {
        LocalDate today = LocalDate.now();
        return switch (timeRange == null ? "this_week" : timeRange) {
            case "today" -> today.atStartOfDay();
            case "this_month" -> today.withDayOfMonth(1).atStartOfDay();
            case "this_year" -> today.with(TemporalAdjusters.firstDayOfYear()).atStartOfDay();
            default -> today.minusDays(today.getDayOfWeek().getValue() - 1L).atStartOfDay();
        };
    }

    private String classifyIntent(String question) {
        String text = question == null ? "" : question;
        if (containsAny(text, "销售", "客户", "商机", "跟进", "成交", "线索")) return "sales";
        if (containsAny(text, "财务", "发票", "对账", "回款", "付款", "金额", "费用")) return "finance";
        if (containsAny(text, "客服", "工单", "投诉", "满意", "交付进度")) return "customer_service";
        if (containsAny(text, "合同", "法务", "风险条款", "违约")) return "contract";
        if (containsAny(text, "复核", "待办", "审批任务", "人工处理", "审批")) return "review";
        return "overview";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private List<MetricCard> metricCards(Object value) {
        if (!(value instanceof List<?> list)) return List.of();
        return list.stream().filter(Map.class::isInstance).map(item -> {
            Map<String, Object> metric = (Map<String, Object>) item;
            return new MetricCard(String.valueOf(metric.get("name")), String.valueOf(metric.get("value")),
                    String.valueOf(metric.get("trend")), String.valueOf(metric.get("status")));
        }).toList();
    }

    private List<String> stringList(Object value) {
        if (!(value instanceof List<?> list)) return List.of();
        return list.stream().map(String::valueOf).toList();
    }
}
