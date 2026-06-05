package com.qyglai.automation.service;

import java.util.List;

import com.qyglai.automation.dto.AutomationModule;
import com.qyglai.automation.dto.BossChatRequest;
import com.qyglai.automation.dto.BossChatResponse;
import com.qyglai.automation.dto.MetricCard;
import org.springframework.stereotype.Service;

@Service
public class MockInsightService {

    public BossChatResponse answer(BossChatRequest request) {
        String intent = classifyIntent(request == null ? "" : request.question());
        if ("sales".equals(intent)) {
            return new BossChatResponse(intent, "销售侧重点看跟进效率：当前待跟进商机 17 个，其中 3 个重点客户超过 3 天未更新。建议先催办逾期客户，再确认 TOP5 商机下一步动作。",
                    List.of(new MetricCard("待跟进商机", "17", "-3", "warning"), new MetricCard("逾期客户", "3", "+1", "warning"), new MetricCard("新增线索", "42", "+9%", "normal"), new MetricCard("高额商机", "5", "持平", "normal")),
                    List.of("催办逾期跟进", "查看TOP5商机", "生成销售跟进清单"),
                    List.of("CRM 商机表", "销售跟进任务", "客户联系记录"));
        }
        if ("finance".equals(intent)) {
            return new BossChatResponse(intent, "财务侧主要风险是对账差异：当前发现 2 笔供应商对账异常，合计差异金额约 8,600 元。建议先核对付款流水和发票号码。",
                    List.of(new MetricCard("对账异常", "2", "持平", "warning"), new MetricCard("差异金额", "8600", "-12%", "warning"), new MetricCard("发票记录", "24", "+6", "normal"), new MetricCard("待付款", "7", "+2", "normal")),
                    List.of("查看对账差异", "核对付款流水", "导出发票明细"),
                    List.of("财务对账单", "发票台账", "付款流水"));
        }
        if ("customer_service".equals(intent)) {
            return new BossChatResponse(intent, "客服侧压力集中在交付进度咨询：高优工单 5 个，比上期增加 2 个；投诉类问题需要客服主管当天闭环。",
                    List.of(new MetricCard("高优工单", "5", "+2", "danger"), new MetricCard("投诉工单", "3", "+1", "danger"), new MetricCard("平均响应", "18分钟", "-4分钟", "normal"), new MetricCard("待处理", "12", "+3", "warning")),
                    List.of("查看高优工单", "通知客服主管", "生成回复建议"),
                    List.of("客服工单系统", "客户反馈记录", "交付排期"));
        }
        if ("contract".equals(intent)) {
            return new BossChatResponse(intent, "合同侧需要关注高金额和风险条款：当前有 4 份合同待复核，其中 1 份金额超过 10 万，2 份包含违约或逾期条款。",
                    List.of(new MetricCard("待复核合同", "4", "+1", "warning"), new MetricCard("高金额合同", "1", "持平", "warning"), new MetricCard("风险条款", "2", "+1", "danger"), new MetricCard("已入库合同", "31", "+5", "normal")),
                    List.of("查看合同风险", "发起法务复核", "导出合同台账"),
                    List.of("合同台账", "AI合同抽取结果", "复核任务"));
        }
        if ("review".equals(intent)) {
            return new BossChatResponse(intent, "待复核事项共 6 个，主要来自合同风险、对账差异和客服投诉。建议按高风险优先处理。",
                    List.of(new MetricCard("待复核", "6", "+2", "warning"), new MetricCard("高风险", "2", "+1", "danger"), new MetricCard("已超时", "1", "+1", "danger"), new MetricCard("今日完成", "8", "+3", "normal")),
                    List.of("查看复核任务", "催办超时任务", "生成复核清单"),
                    List.of("复核任务池", "合同台账", "财务对账单", "客服工单"));
        }
        if ("report".equals(intent)) {
            return new BossChatResponse(intent, "可以生成经营报表。建议包含销售进展、合同风险、财务对账、客服质量和待办复核五部分，并突出异常、责任人和截止时间。",
                    List.of(new MetricCard("报表章节", "5", "标准模板", "normal"), new MetricCard("异常事项", "7", "+2", "warning"), new MetricCard("数据来源", "4", "已连接", "normal"), new MetricCard("待补充", "1", "客服SLA", "warning")),
                    List.of("生成经营日报", "生成周报", "导出老板摘要"),
                    List.of("CRM", "合同台账", "财务系统", "客服工单系统"));
        }
        return new BossChatResponse(intent,
                "整体经营状态可控，但有三类事项需要关注：销售有 3 个重点客户逾期未跟进，财务有 2 笔对账差异，客服高优工单增加。建议先处理会影响回款和客户满意度的事项。",
                List.of(new MetricCard("新增客户", "38", "+12%", "normal"), new MetricCard("待跟进商机", "17", "-3", "warning"), new MetricCard("对账异常", "2", "持平", "warning"), new MetricCard("高优工单", "5", "+2", "danger")),
                List.of("催办逾期跟进", "查看对账差异", "查看高优工单"),
                List.of("CRM 商机表", "财务对账单", "客服工单系统", "合同台账"));
    }

    private String classifyIntent(String question) {
        if (containsAny(question, "销售", "客户", "商机", "跟进", "成交", "线索")) return "sales";
        if (containsAny(question, "财务", "发票", "对账", "回款", "付款", "金额", "费用")) return "finance";
        if (containsAny(question, "客服", "工单", "投诉", "满意", "交付进度")) return "customer_service";
        if (containsAny(question, "合同", "法务", "风险条款", "审批", "违约")) return "contract";
        if (containsAny(question, "复核", "待办", "审批任务", "人工处理")) return "review";
        if (containsAny(question, "日报", "周报", "月报", "报表", "导出", "汇总")) return "report";
        return "overview";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text != null && text.contains(keyword)) return true;
        }
        return false;
    }

    public List<AutomationModule> modules() {
        return List.of(
                new AutomationModule("contract", "合同信息提取", "抽取合同主体、金额、期限、付款节点和风险条款", "法务/行政", "enabled"),
                new AutomationModule("invoice", "发票与对账处理", "识别发票、查重、匹配对账单并标记差异", "财务", "enabled"),
                new AutomationModule("ticket", "客服工单归类", "识别类型、优先级、情绪和建议回复", "客服", "enabled"),
                new AutomationModule("sales", "销售跟进提醒", "从客户动态中生成下一步动作和逾期提醒", "销售", "enabled"),
                new AutomationModule("kb", "知识库问答", "基于企业资料回答问题并返回引用来源", "运营", "enabled"),
                new AutomationModule("report", "日报周报生成", "自动汇总进展、风险、待办和经营指标", "管理层", "enabled")
        );
    }
}
