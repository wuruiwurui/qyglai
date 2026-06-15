package com.qyglai.automation.service;

import com.qyglai.automation.dto.AiEvaluationDashboard;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * AI 效果评估服务。
 *
 * <p>基于模型调用日志、字段修正历史、人工复核结果和工作流轨迹生成实时评估指标。</p>
 */
@Service
public class AiEvaluationService {

    private final JdbcTemplate jdbcTemplate;

    public AiEvaluationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 查询 AI 效果评估中心数据。 */
    public AiEvaluationDashboard dashboard(int requestedDays) {
        int days = normalizeDays(requestedDays);
        String callWhere = where(days, "created_at");
        String correctionWhere = where(days, "created_at");
        String reviewWhere = where(days, "created_at");
        String workflowWhere = where(days, "created_at");
        Map<String, Object> callSummary = one("""
                SELECT COUNT(*) total_calls,
                       COALESCE(SUM(CASE WHEN success_flag=1 THEN 1 ELSE 0 END),0) successful_calls,
                       COALESCE(AVG(latency_ms),0) average_latency,
                       COALESCE(SUM(request_tokens + response_tokens),0) total_tokens
                FROM ai_model_call_log
                """ + callWhere);
        long totalCalls = number(callSummary, "total_calls");
        long successfulCalls = number(callSummary, "successful_calls");
        AiEvaluationDashboard.Overview overview = new AiEvaluationDashboard.Overview(
                totalCalls, successfulCalls, rate(successfulCalls, totalCalls),
                number(callSummary, "average_latency"), number(callSummary, "total_tokens"),
                Math.max(0, totalCalls - successfulCalls));

        List<AiEvaluationDashboard.PerformanceItem> models = performance("""
                SELECT COALESCE(NULLIF(model_name,''),'未记录模型') item_name,
                       COUNT(*) calls,
                       SUM(CASE WHEN success_flag=1 THEN 1 ELSE 0 END) successful_calls,
                       AVG(latency_ms) average_latency,
                       SUM(request_tokens + response_tokens) total_tokens
                FROM ai_model_call_log
                %s
                GROUP BY COALESCE(NULLIF(model_name,''),'未记录模型')
                ORDER BY calls DESC
                """.formatted(callWhere));
        List<AiEvaluationDashboard.PerformanceItem> scenarios = performance("""
                SELECT COALESCE(NULLIF(scenario,''),'未分类场景') item_name,
                       COUNT(*) calls,
                       SUM(CASE WHEN success_flag=1 THEN 1 ELSE 0 END) successful_calls,
                       AVG(latency_ms) average_latency,
                       SUM(request_tokens + response_tokens) total_tokens
                FROM ai_model_call_log
                %s
                GROUP BY COALESCE(NULLIF(scenario,''),'未分类场景')
                ORDER BY calls DESC
                """.formatted(callWhere));
        List<AiEvaluationDashboard.FieldCorrectionItem> corrections = jdbcTemplate.query("""
                SELECT field_key, COALESCE(NULLIF(field_name,''), field_key) field_name,
                       COUNT(*) correction_count,
                       COUNT(DISTINCT file_id) affected_files,
                       COUNT(DISTINCT business_id) affected_businesses
                FROM field_correction_history
                %s
                GROUP BY field_key, COALESCE(NULLIF(field_name,''), field_key)
                ORDER BY correction_count DESC
                LIMIT 12
                """.formatted(correctionWhere), (rs, rowNum) -> new AiEvaluationDashboard.FieldCorrectionItem(
                rs.getString("field_key"), rs.getString("field_name"), rs.getLong("correction_count"),
                rs.getLong("affected_files"), rs.getLong("affected_businesses")));

        Map<String, Object> correctionSummary = one("""
                SELECT COUNT(*) correction_count, COUNT(DISTINCT batch_id) correction_batches,
                       COUNT(DISTINCT file_id) affected_files
                FROM field_correction_history
                """ + correctionWhere);
        Map<String, Object> reviewSummary = one("""
                SELECT COUNT(*) review_tasks,
                       SUM(CASE WHEN status='completed' THEN 1 ELSE 0 END) completed_reviews,
                       SUM(CASE WHEN review_result LIKE '%humanResult%' AND review_result LIKE '%aiAdvice%' THEN 1 ELSE 0 END) feedback_samples
                FROM review_task
                """ + reviewWhere);
        Map<String, Object> workflowSummary = one("""
                SELECT SUM(CASE WHEN action='ai_auto_approved' THEN 1 ELSE 0 END) ai_auto_approved,
                       SUM(CASE WHEN action='ai_manual_review' THEN 1 ELSE 0 END) ai_manual_review
                FROM workflow_action_log
                """ + workflowWhere);
        long autoApproved = number(workflowSummary, "ai_auto_approved");
        long manualReview = number(workflowSummary, "ai_manual_review");
        AiEvaluationDashboard.Feedback feedback = new AiEvaluationDashboard.Feedback(
                number(correctionSummary, "correction_count"), number(correctionSummary, "correction_batches"),
                number(correctionSummary, "affected_files"), number(reviewSummary, "review_tasks"),
                number(reviewSummary, "completed_reviews"), number(reviewSummary, "feedback_samples"),
                autoApproved, manualReview, rate(autoApproved, autoApproved + manualReview));
        return new AiEvaluationDashboard(overview, models, scenarios, corrections, feedback,
                recommendations(overview, models, scenarios, corrections, feedback));
    }

    private List<AiEvaluationDashboard.Recommendation> recommendations(
            AiEvaluationDashboard.Overview overview,
            List<AiEvaluationDashboard.PerformanceItem> models,
            List<AiEvaluationDashboard.PerformanceItem> scenarios,
            List<AiEvaluationDashboard.FieldCorrectionItem> corrections,
            AiEvaluationDashboard.Feedback feedback) {
        List<AiEvaluationDashboard.Recommendation> items = new ArrayList<>();
        if (overview.totalCalls() == 0) {
            items.add(new AiEvaluationDashboard.Recommendation("info", "尚无真实调用样本",
                    "当前时间范围内没有模型调用数据，暂时无法判断模型质量。", "先执行模型健康检查并运行核心业务场景"));
            return items;
        }
        if (overview.successRate() < 95) items.add(new AiEvaluationDashboard.Recommendation("high", "模型调用成功率偏低",
                "当前成功率为 " + overview.successRate() + "%，可能影响业务自动化稳定性。", "检查异常模型并配置健康的备用模型"));
        if (overview.averageLatencyMs() > 8000) items.add(new AiEvaluationDashboard.Recommendation("medium", "模型平均响应较慢",
                "平均耗时 " + overview.averageLatencyMs() + "ms，交互场景可能出现明显等待。", "为交互场景选择低延迟模型"));
        scenarios.stream().filter(item -> item.calls() >= 3 && item.successRate() < 90).findFirst().ifPresent(item ->
                items.add(new AiEvaluationDashboard.Recommendation("high", item.name() + " 场景稳定性不足",
                        "该场景成功率仅为 " + item.successRate() + "%。", "单独配置场景主模型并增加备用路由")));
        models.stream().filter(item -> item.calls() >= 3 && item.successRate() < 90).findFirst().ifPresent(item ->
                items.add(new AiEvaluationDashboard.Recommendation("medium", item.name() + " 模型需要关注",
                        "该模型近期成功率为 " + item.successRate() + "%。", "执行健康检查，必要时停止用于关键场景")));
        if (!corrections.isEmpty()) {
            AiEvaluationDashboard.FieldCorrectionItem top = corrections.getFirst();
            items.add(new AiEvaluationDashboard.Recommendation("medium", top.fieldName() + " 是高频修正字段",
                    "该字段已被人工修正 " + top.correctionCount() + " 次。", "优化抽取提示词、规则和字段校验"));
        }
        if (feedback.reviewTasks() > 0 && feedback.completedReviews() * 1.0 / feedback.reviewTasks() < 0.7) {
            items.add(new AiEvaluationDashboard.Recommendation("medium", "人工复核闭环率偏低",
                    "仍有较多AI结果未完成人工确认。", "优先处理复核队列并沉淀有效反馈样本"));
        }
        if (items.isEmpty()) items.add(new AiEvaluationDashboard.Recommendation("low", "AI运行状态稳定",
                "当前成功率、耗时和业务反馈未发现明显异常。", "继续积累真实样本并定期复盘"));
        return items.stream().limit(6).toList();
    }

    private List<AiEvaluationDashboard.PerformanceItem> performance(String sql) {
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            long calls = rs.getLong("calls");
            long successes = rs.getLong("successful_calls");
            return new AiEvaluationDashboard.PerformanceItem(rs.getString("item_name"), calls, successes,
                    rate(successes, calls), rs.getLong("average_latency"), rs.getLong("total_tokens"));
        });
    }

    private Map<String, Object> one(String sql) {
        return jdbcTemplate.queryForMap(sql);
    }

    private long number(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value instanceof Number number ? number.longValue() : 0L;
    }

    private double rate(long numerator, long denominator) {
        return denominator == 0 ? 0.0 : Math.round(numerator * 10000.0 / denominator) / 100.0;
    }

    private int normalizeDays(int days) {
        return List.of(0, 7, 30, 90).contains(days) ? days : 30;
    }

    private String where(int days, String column) {
        return days == 0 ? "" : " WHERE " + column + " >= DATE_SUB(NOW(), INTERVAL " + days + " DAY) ";
    }
}
