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

    public JavaAiTaskService(JavaAiModelGateway modelGateway) {
        this.modelGateway = modelGateway;
    }

    public SalesFollowupAdviceResponse salesFollowup(SalesFollowupAdviceRequest request) {
        BigDecimal amount = request.amount() == null ? BigDecimal.ZERO : request.amount();
        boolean highValue = amount.compareTo(new BigDecimal("100000")) >= 0;
        boolean overdue = request.lastContactDays() >= 3;
        String priority = highValue && overdue ? "P1" : overdue ? "P2" : "P3";
        int dueHours = "P1".equals(priority) ? 4 : "P2".equals(priority) ? 24 : 72;
        String fallback = highValue ? "电话确认决策人、预算和预计签约时间" : "发送跟进信息并预约下一次沟通";
        String nextAction = modelGateway.generateText(
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
        List<String> risks = new ArrayList<>();
        if (difference.signum() != 0) risks.add("对账单、发票与付款金额存在差异");
        if (statement.compareTo(new BigDecimal("100000")) > 0) risks.add("金额较大，建议财务主管复核");
        List<String> suggestions = risks.isEmpty()
                ? List.of("金额匹配，可进入后续付款或归档流程")
                : List.of("核对发票号码", "确认付款流水", "联系供应商补充差异说明");
        return new ReconciliationAnalyzeResponse(risks.isEmpty() ? "matched" : "difference", difference, risks, suggestions);
    }

    public ReviewAdviceResponse review(ReviewAdviceRequest request) {
        String content = request.content() == null ? "" : request.content();
        boolean highRisk = "high".equalsIgnoreCase(request.riskLevel())
                || containsAny(content, "违约", "逾期", "差异", "投诉", "赔偿");
        return new ReviewAdviceResponse(highRisk ? "need_review" : "auto_pass",
                highRisk ? List.of("存在高风险等级或风险关键词") : List.of("内容完整且未命中高风险规则"),
                List.of("核对金额", "核对主体", "确认审批链路", "记录处理结论"),
                highRisk ? 0.91 : 0.86);
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
