package com.qyglai.automation.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.qyglai.automation.dto.PromptEvaluateRequest;
import com.qyglai.automation.dto.PromptEvaluateResponse;
import com.qyglai.automation.dto.ReconciliationAnalyzeRequest;
import com.qyglai.automation.dto.ReconciliationAnalyzeResponse;
import com.qyglai.automation.dto.ReviewAdviceRequest;
import com.qyglai.automation.dto.ReviewAdviceResponse;
import com.qyglai.automation.dto.SalesFollowupAdviceRequest;
import com.qyglai.automation.dto.SalesFollowupAdviceResponse;
import org.springframework.stereotype.Service;

/**
 * Java AI 轻量任务服务。
 *
 * <p>确定性结论由 Java 规则计算，豆包仅负责优化自然语言建议。</p>
 */
@Service
public class JavaAiTaskService {

    private final JavaAiModelGateway modelGateway;
    private final AiBusinessApplicationService aiBusinessApplicationService;

    public JavaAiTaskService(JavaAiModelGateway modelGateway, AiBusinessApplicationService aiBusinessApplicationService) {
        this.modelGateway = modelGateway;
        this.aiBusinessApplicationService = aiBusinessApplicationService;
    }

    public SalesFollowupAdviceResponse salesFollowup(SalesFollowupAdviceRequest request) {
        BigDecimal amount = request.amount() == null ? BigDecimal.ZERO : request.amount();
        boolean highValue = amount.compareTo(new BigDecimal("100000")) >= 0;
        boolean overdue = request.lastContactDays() >= 3;
        String priority = highValue && overdue ? "P1" : overdue ? "P2" : "P3";
        int dueHours = "P1".equals(priority) ? 4 : "P2".equals(priority) ? 24 : 72;
        String fallback = highValue ? "电话确认决策人、预算和预计签约时间" : "发送跟进信息并预约下一次沟通";
        String nextAction = modelGateway.generateText("sales_followup",
                "你是销售跟进助手。仅根据提供的数据输出一句明确的下一步动作，不得编造客户信息。",
                "客户：" + request.customerName() + "，阶段：" + request.opportunityStage()
                        + "，未联系天数：" + request.lastContactDays() + "，商机金额：" + amount,
                fallback);
        return new SalesFollowupAdviceResponse(priority, nextAction, dueHours,
                request.customerName() + "当前处于" + request.opportunityStage() + "阶段，建议在" + dueHours + "小时内跟进。");
    }

    public ReconciliationAnalyzeResponse reconciliation(ReconciliationAnalyzeRequest request) {
        BigDecimal statement = value(request.statementAmount());
        BigDecimal invoice = value(request.invoiceAmount());
        BigDecimal paid = value(request.paidAmount());
        BigDecimal difference = statement.subtract(invoice).subtract(paid).setScale(2, RoundingMode.HALF_UP);
        AiBusinessApplicationService.ReconciliationDecision decision =
                aiBusinessApplicationService.explainReconciliation(request.supplierName(), statement,
                        invoice.add(paid), difference);
        return new ReconciliationAnalyzeResponse(decision.status(), difference, decision.risks(), decision.suggestions());
    }

    public ReviewAdviceResponse review(ReviewAdviceRequest request) {
        AiBusinessApplicationService.ReviewDecision decision =
                aiBusinessApplicationService.adviseReview(request.scenario(), request.content(), request.riskLevel());
        return new ReviewAdviceResponse(decision.decision(), decision.reasons(), decision.checklist(), decision.confidence());
    }

    public PromptEvaluateResponse evaluatePrompt(PromptEvaluateRequest request) {
        String prompt = request.prompt() == null ? "" : request.prompt();
        List<String> issues = new ArrayList<>();
        if (!prompt.contains("{input}")) issues.add("缺少输入变量占位符 {input}");
        if (prompt.length() < 20) issues.add("提示词过短，约束不够明确");
        if (!containsAny(prompt, "格式", "JSON", "结构化", "列表")) issues.add("缺少输出格式约束");
        double score = Math.max(0.40, 0.96 - issues.size() * 0.16);
        return new PromptEvaluateResponse(score, issues,
                List.of("明确输出格式", "补充角色和边界条件", "要求列出数据来源"));
    }

    private BigDecimal value(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private boolean containsAny(String text, String... values) {
        for (String value : values) if (text.contains(value)) return true;
        return false;
    }
}
