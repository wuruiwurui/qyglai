package com.qyglai.automation.dto;

import java.util.List;

/**
 * AI 效果评估中心聚合数据。
 *
 * @param overview 核心评估指标
 * @param modelPerformance 不同模型表现
 * @param scenarioPerformance 不同业务场景表现
 * @param fieldCorrections 高频人工修正字段
 * @param feedback 业务反馈闭环指标
 */
public record AiEvaluationDashboard(
        Overview overview,
        List<PerformanceItem> modelPerformance,
        List<PerformanceItem> scenarioPerformance,
        List<FieldCorrectionItem> fieldCorrections,
        Feedback feedback
) {
    /** 核心调用与质量指标。 */
    public record Overview(long totalCalls, long successfulCalls, double successRate,
                           long averageLatencyMs, long totalTokens, long failedCalls) {
    }

    /** 模型或场景维度调用表现。 */
    public record PerformanceItem(String name, long calls, long successfulCalls, double successRate,
                                  long averageLatencyMs, long totalTokens) {
    }

    /** 字段人工修正统计。 */
    public record FieldCorrectionItem(String fieldKey, String fieldName, long correctionCount,
                                      long affectedFiles, long affectedBusinesses) {
    }

    /** AI 输出进入业务后的反馈闭环指标。 */
    public record Feedback(long correctionCount, long correctionBatches, long affectedFiles,
                           long reviewTasks, long completedReviews, long feedbackSamples,
                           long aiAutoApproved, long aiManualReview, double aiAutoApprovalRate) {
    }
}
