package com.qyglai.automation.service;

import com.qyglai.automation.dto.AiEvaluationDashboard;
import java.util.List;
import java.util.Map;
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
        return new AiEvaluationDashboard(overview, models, scenarios, corrections, feedback);
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
