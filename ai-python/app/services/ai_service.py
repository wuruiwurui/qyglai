"""AI 业务服务层。"""

import re

from app.core.settings import settings
from app.schemas.ai import (
    BossQueryRequest,
    BossQueryResponse,
    ClassificationResponse,
    ExtractionResponse,
    KnowledgeAnswer,
    KnowledgeQueryRequest,
    MetricCard,
    PromptEvaluateRequest,
    PromptEvaluateResponse,
    ReconciliationRequest,
    ReconciliationResponse,
    ReportRequest,
    ReportResponse,
    ReviewAdviceRequest,
    ReviewAdviceResponse,
    SalesFollowupRequest,
    SalesFollowupResponse,
    TextTaskRequest,
)
from app.services.model_provider import model_provider
from app.services.rule_engine import rule_engine


class EnterpriseAiService:
    """企业流程自动化 AI 服务。

    当前采用本地规则生成稳定结果；真实大模型接入时，可在这里抽象 provider 调用。
    """

    def boss_query(self, payload: BossQueryRequest) -> BossQueryResponse:
        """根据老板自然语言问题生成可执行回答。"""

        time_range = payload.timeRange or "this_week"
        intent = self._classify_boss_intent(payload.question)
        fallback, metrics, actions, sources = self._boss_answer_payload(intent, payload.question, time_range)
        answer = model_provider.generate_text(
            "你是企业老板的经营助手。必须先判断问题意图，再用中文给出直接结论、关键数字、风险和下一步动作，不要把所有问题都回答成报表。",
            f"问题：{payload.question}\n时间范围：{time_range}\n识别意图：{intent}",
            fallback,
        )
        return BossQueryResponse(
            intent=intent,
            answer=answer,
            metrics=metrics,
            actions=actions,
            sources=sources,
        )

    def extract_document(self, payload: TextTaskRequest) -> ExtractionResponse:
        """抽取合同、发票或通用文档字段。"""

        text = rule_engine.normalize(payload.content)
        amounts = rule_engine.extract_amounts(text)
        scenario = (payload.scenario or "general").lower()
        if scenario in {"general", "contract"} and self._looks_like_invoice(text):
            scenario = "invoice"
        amount = self._select_business_amount(amounts, scenario, text)
        confidence = rule_engine.confidence(0.87, text)
        risks = self._document_risks(text, amount)

        fields = {
            "scenario": scenario,
            "amount": f"{amount:.2f}" if amount else "未识别",
            "party_a": "示例甲方有限公司",
            "party_b": "示例乙方有限公司",
            "payment_terms": "30天" if "30" in text else "待确认",
        }
        if scenario == "invoice":
            fields.update(self._extract_invoice_fields(text, amount))

        return ExtractionResponse(
            scenario=scenario,
            confidence=confidence,
            fields=fields,
            risks=risks,
            reviewRequired=confidence < settings.human_review_threshold or bool(risks),
        )

    def classify_ticket(self, payload: TextTaskRequest) -> ClassificationResponse:
        """对客服工单分类并生成建议回复。"""

        text = rule_engine.normalize(payload.content)
        if rule_engine.contains_any(text, ["投诉", "不满", "很慢", "差"]):
            priority, sentiment = "P1", "negative"
        elif rule_engine.contains_any(text, ["进度", "排期", "交付"]):
            priority, sentiment = "P2", "neutral"
        else:
            priority, sentiment = "P3", "neutral"

        category = "交付进度咨询" if "进度" in text or "交付" in text else "常规咨询"
        return ClassificationResponse(
            category=category,
            priority=priority,
            sentiment=sentiment,
            suggestedOwner="客服主管" if priority in {"P1", "P2"} else "一线客服",
            confidence=rule_engine.confidence(0.84, text),
            suggestedReply="已收到您的反馈，我们会同步项目负责人确认最新排期，并在今天内给出明确回复。",
        )

    def generate_report(self, payload: ReportRequest | TextTaskRequest) -> ReportResponse:
        """生成日报、周报或经营摘要。"""

        report_type = getattr(payload, "reportType", None) or "daily"
        time_range = getattr(payload, "timeRange", None) or "today"
        fallback_summary = "销售新增客户稳定，客服高优工单上升，财务存在少量对账异常，建议管理层关注逾期跟进和差异处理。"
        summary = model_provider.generate_text(
            "你是企业报表助手，根据输入生成经营摘要，语言必须专业、克制、可执行。",
            f"生成{report_type}报表，时间范围{time_range}",
            fallback_summary,
        )
        return ReportResponse(
            title=f"{report_type} - {time_range}",
            summary=summary,
            sections=["销售进展", "合同风险", "财务对账", "客服质量", "明日重点"],
            sources=["CRM", "合同台账", "财务系统", "客服工单系统"],
            status="draft",
        )

    def knowledge_query(self, payload: KnowledgeQueryRequest) -> KnowledgeAnswer:
        """模拟企业知识库问答。"""

        confidence = 0.89 if "合同" in payload.question or "审批" in payload.question else 0.78
        return KnowledgeAnswer(
            answer="根据企业制度，合同金额超过10万元需部门负责人、法务和财务复核；超过50万元需总经理审批。",
            confidence=confidence,
            citations=[f"{payload.scope}/合同审批制度", "法务审核清单"],
            humanHandoffSuggested=confidence < settings.human_review_threshold,
        )

    def review_advice(self, payload: ReviewAdviceRequest) -> ReviewAdviceResponse:
        """给人工复核人员生成处理建议。"""

        text = rule_engine.normalize(payload.content)
        high_risk = payload.riskLevel == "high" or rule_engine.contains_any(text, ["违约", "超期", "差异", "投诉"])
        return ReviewAdviceResponse(
            decision="need_review" if high_risk else "auto_pass",
            reasons=["存在风险关键词或高风险等级"] if high_risk else ["内容完整且未命中高风险规则"],
            checklist=["核对金额", "核对主体", "确认审批链路", "记录处理结论"],
            confidence=rule_engine.confidence(0.86, text),
        )

    def sales_followup(self, payload: SalesFollowupRequest) -> SalesFollowupResponse:
        """生成销售跟进提醒。"""

        high_value = (payload.amount or 0) >= 100000
        overdue = payload.lastContactDays >= 3
        priority = "P1" if high_value and overdue else "P2" if overdue else "P3"
        return SalesFollowupResponse(
            priority=priority,
            nextAction="电话确认决策人和预算时间" if high_value else "发送跟进邮件并预约下一次沟通",
            dueHours=4 if priority == "P1" else 24,
            message=f"{payload.customerName} 当前处于{payload.opportunityStage}阶段，建议尽快跟进。",
        )

    def analyze_reconciliation(self, payload: ReconciliationRequest) -> ReconciliationResponse:
        """分析供应商对账差异。"""

        difference = round(payload.statementAmount - payload.invoiceAmount - payload.paidAmount, 2)
        risks: list[str] = []
        if abs(difference) > 0:
            risks.append("对账单、发票与付款金额存在差异")
        if payload.statementAmount > 100000:
            risks.append("金额较大，建议财务主管复核")
        return ReconciliationResponse(
            status="matched" if not risks else "difference",
            difference=difference,
            risks=risks,
            suggestions=["核对发票号码", "确认付款流水", "联系供应商补充明细"] if risks else ["可进入付款流程"],
        )

    def evaluate_prompt(self, payload: PromptEvaluateRequest) -> PromptEvaluateResponse:
        """评测 Prompt 模板质量。"""

        issues: list[str] = []
        if "{input}" not in payload.prompt:
            issues.append("缺少输入变量占位符 {input}")
        if len(payload.prompt) < 20:
            issues.append("提示词过短，约束不够明确")
        score = max(0.55, 0.95 - len(issues) * 0.18)
        return PromptEvaluateResponse(
            score=round(score, 2),
            issues=issues,
            suggestions=["明确输出格式", "补充角色和边界条件", "要求列出数据来源"],
        )

    def _classify_boss_intent(self, question: str) -> str:
        """把老板自然语言问题归类到业务场景。"""

        text = rule_engine.normalize(question)
        if rule_engine.contains_any(text, ["销售", "客户", "商机", "跟进", "成交", "线索"]):
            return "sales"
        if rule_engine.contains_any(text, ["财务", "发票", "对账", "回款", "付款", "金额", "费用"]):
            return "finance"
        if rule_engine.contains_any(text, ["客服", "工单", "投诉", "满意", "交付进度"]):
            return "customer_service"
        if rule_engine.contains_any(text, ["合同", "法务", "风险条款", "审批", "违约"]):
            return "contract"
        if rule_engine.contains_any(text, ["复核", "待办", "审批任务", "人工处理"]):
            return "review"
        if rule_engine.contains_any(text, ["日报", "周报", "月报", "报表", "导出", "汇总"]):
            return "report"
        return "overview"

    def _boss_answer_payload(self, intent: str, question: str, time_range: str) -> tuple[str, list[MetricCard], list[str], list[str]]:
        """按问题意图组织本地兜底回答、指标、建议动作和数据来源。"""

        if intent == "sales":
            return (
                f"销售侧{time_range}重点看跟进效率：当前待跟进商机 17 个，其中 3 个重点客户超过 3 天未更新；高金额商机主要停留在报价和谈判阶段。建议先催办逾期客户，再让销售负责人确认 TOP5 商机下一步动作。",
                [
                    MetricCard(name="待跟进商机", value="17", trend="-3", status="warning"),
                    MetricCard(name="逾期客户", value="3", trend="+1", status="warning"),
                    MetricCard(name="新增线索", value="42", trend="+9%", status="normal"),
                    MetricCard(name="高额商机", value="5", trend="持平", status="normal"),
                ],
                ["催办逾期跟进", "查看TOP5商机", "生成销售跟进清单"],
                ["CRM 商机表", "销售跟进任务", "客户联系记录"],
            )
        if intent == "finance":
            return (
                f"财务侧{time_range}主要风险是对账差异：当前发现 2 笔供应商对账异常，合计差异金额约 8,600 元；发票识别记录整体正常，建议财务先核对付款流水和发票号码。",
                [
                    MetricCard(name="对账异常", value="2", trend="持平", status="warning"),
                    MetricCard(name="差异金额", value="8600", trend="-12%", status="warning"),
                    MetricCard(name="发票记录", value="24", trend="+6", status="normal"),
                    MetricCard(name="待付款", value="7", trend="+2", status="normal"),
                ],
                ["查看对账差异", "核对付款流水", "导出发票明细"],
                ["财务对账单", "发票台账", "付款流水"],
            )
        if intent == "customer_service":
            return (
                f"客服侧{time_range}压力集中在交付进度咨询：高优工单 5 个，比上期增加 2 个；投诉类问题需要客服主管当天闭环，避免继续升级。",
                [
                    MetricCard(name="高优工单", value="5", trend="+2", status="danger"),
                    MetricCard(name="投诉工单", value="3", trend="+1", status="danger"),
                    MetricCard(name="平均响应", value="18分钟", trend="-4分钟", status="normal"),
                    MetricCard(name="待处理", value="12", trend="+3", status="warning"),
                ],
                ["查看高优工单", "通知客服主管", "生成回复建议"],
                ["客服工单系统", "客户反馈记录", "交付排期"],
            )
        if intent == "contract":
            return (
                f"合同侧{time_range}需要关注高金额和风险条款：当前有 4 份合同待复核，其中 1 份金额超过 10 万，2 份包含违约或逾期条款。建议优先交给法务确认风险边界。",
                [
                    MetricCard(name="待复核合同", value="4", trend="+1", status="warning"),
                    MetricCard(name="高金额合同", value="1", trend="持平", status="warning"),
                    MetricCard(name="风险条款", value="2", trend="+1", status="danger"),
                    MetricCard(name="已入库合同", value="31", trend="+5", status="normal"),
                ],
                ["查看合同风险", "发起法务复核", "导出合同台账"],
                ["合同台账", "AI合同抽取结果", "复核任务"],
            )
        if intent == "review":
            return (
                f"{time_range}待复核事项共 6 个，主要来自合同风险、对账差异和客服投诉。建议按高风险优先处理：先处理金额和客户影响较大的任务，再处理普通资料补全。",
                [
                    MetricCard(name="待复核", value="6", trend="+2", status="warning"),
                    MetricCard(name="高风险", value="2", trend="+1", status="danger"),
                    MetricCard(name="已超时", value="1", trend="+1", status="danger"),
                    MetricCard(name="今日完成", value="8", trend="+3", status="normal"),
                ],
                ["查看复核任务", "催办超时任务", "生成复核清单"],
                ["复核任务池", "合同台账", "财务对账单", "客服工单"],
            )
        if intent == "report":
            return (
                f"可以生成{time_range}经营报表。建议报表包含销售进展、合同风险、财务对账、客服质量和待办复核五部分，并突出异常、责任人和截止时间。",
                [
                    MetricCard(name="报表章节", value="5", trend="标准模板", status="normal"),
                    MetricCard(name="异常事项", value="7", trend="+2", status="warning"),
                    MetricCard(name="数据来源", value="4", trend="已连接", status="normal"),
                    MetricCard(name="待补充", value="1", trend="客服SLA", status="warning"),
                ],
                ["生成经营日报", "生成周报", "导出老板摘要"],
                ["CRM", "合同台账", "财务系统", "客服工单系统"],
            )
        return (
            f"{time_range}整体经营状态可控，但有三类事项需要关注：销售有 3 个重点客户逾期未跟进，财务有 2 笔对账差异，客服高优工单增加。建议先处理会影响回款和客户满意度的事项。",
            [
                MetricCard(name="新增客户", value="38", trend="+12%", status="normal"),
                MetricCard(name="待跟进商机", value="17", trend="-3", status="warning"),
                MetricCard(name="对账异常", value="2", trend="持平", status="warning"),
                MetricCard(name="高优工单", value="5", trend="+2", status="danger"),
            ],
            ["催办逾期跟进", "查看对账差异", "查看高优工单"],
            ["CRM 商机表", "财务对账单", "客服工单系统", "合同台账"],
        )

    def _document_risks(self, text: str, amount: float) -> list[str]:
        """识别文档风险点。"""

        risks: list[str] = []
        if amount >= 100000:
            risks.append("金额超过10万元，建议进入人工复核。")
        if rule_engine.contains_any(text, ["违约", "赔偿", "逾期"]):
            risks.append("存在违约或赔偿条款，需要法务确认。")
        if rule_engine.contains_any(text, ["投诉", "差异"]):
            risks.append("存在投诉或差异信息，建议主管复核。")
        return risks

    def _select_business_amount(self, amounts, scenario: str, text: str) -> float:
        """选择最可能代表业务金额的数字。

        发票号码、合同编号中也会包含长数字，因此优先选择带“元/万”单位的金额。
        """

        if scenario == "invoice":
            invoice_total = self._extract_invoice_total_amount(text)
            if invoice_total:
                return invoice_total
        keyword_match = re.search(r"(?:金额|费用|服务费|合同金额|价税合计)[^\d]{0,8}(\d+(?:\.\d+)?)", text)
        if keyword_match:
            return float(keyword_match.group(1))
        yuan_match = re.search(r"(\d+(?:\.\d+)?)\s*元", text)
        if yuan_match:
            return float(yuan_match.group(1))
        wan_match = re.search(r"(\d+(?:\.\d+)?)\s*万", text)
        if wan_match:
            return float(wan_match.group(1)) * 10000
        if not amounts:
            return 0
        unit_amounts = [item for item in amounts if item.raw.endswith("元") or item.raw.endswith("万")]
        if unit_amounts:
            return unit_amounts[0].value
        if scenario == "invoice":
            candidates = [item.value for item in amounts if 1 < item.value < 100000000]
            return candidates[-1] if candidates else amounts[0].value
        return amounts[0].value

    def _looks_like_invoice(self, text: str) -> bool:
        """判断文本是否明显是发票，避免业务类型选错时使用合同金额规则。"""

        return rule_engine.contains_any(text, ["发票", "税率", "税额", "价税合计", "购买方", "销售方"])

    def _extract_invoice_total_amount(self, text: str) -> float:
        """提取发票价税合计金额。

        电子发票 PDF 中常见航班号、订单号、税号等数字，例如 MU9188。
        这里优先选择人民币符号后的金额，并在多个 ¥ 金额中取最大值，通常对应价税合计。
        """

        total_match = re.search(r"(?:价税合计|小写|合计)[^¥￥]{0,120}[¥￥]\s*(\d{1,12}(?:\.\d{1,2})?)", text)
        if total_match:
            return float(total_match.group(1))
        currency_values = [
            float(value)
            for value in re.findall(r"[¥￥]\s*(\d{1,12}(?:\.\d{1,2})?)", text)
        ]
        if currency_values:
            return max(currency_values)
        total_number_match = re.search(r"(?:价税合计|小写|合计)[^\d]{0,80}(\d{1,12}\.\d{2})", text)
        if total_number_match:
            return float(total_number_match.group(1))
        return 0

    def _extract_invoice_fields(self, text: str, total_amount: float) -> dict[str, str]:
        """抽取电子发票核心字段。"""

        currency_values = [
            float(value)
            for value in re.findall(r"[¥￥]\s*(\d{1,12}(?:\.\d{1,2})?)", text)
        ]
        line_amount = 0.0
        tax_amount = 0.0
        if currency_values:
            sorted_values = sorted(currency_values, reverse=True)
            line_amount = sorted_values[1] if len(sorted_values) > 1 else total_amount
            tax_amount = sorted_values[2] if len(sorted_values) > 2 else max(total_amount - line_amount, 0)
        else:
            service_line = re.search(r"\s(\d{1,12}\.\d{2})\s+(\d{1,2})%\s+(\d{1,12}\.\d{2})", text)
            if service_line:
                line_amount = float(service_line.group(1))
                tax_amount = float(service_line.group(3))

        invoice_no = self._first_match(text, [
            r"发票号码[:：]?\s*(\d{8,30})",
            r"(?<!\d)(\d{20})(?!\d)",
        ], "FP-AI-20260604")
        invoice_date = self._normalize_invoice_date(self._first_match(text, [r"(\d{4}年\d{1,2}月\d{1,2}日)"], ""))
        tax_numbers = [
            item
            for item in re.findall(r"(?<![A-Z0-9])([0-9A-Z]{15,20})(?![A-Z0-9])", text)
            if item != invoice_no
        ]
        companies = re.findall(r"([\u4e00-\u9fa5A-Za-z0-9（）()]{2,40}(?:公司|分公司|旅行社|企业|中心|商行|工作室))", text)
        order_no = self._first_match(text, [r"携程订单[:：]\s*([0-9]+)", r"订单[:：]\s*([0-9]+)"], "")
        flight = self._first_match(text, [r"(\d{4}/\d{1,2}/\d{1,2}\s+[\u4e00-\u9fa5]+-[\u4e00-\u9fa5]+\s+[A-Z]{2}\d{3,5})"], "")

        seller_name = companies[0] if companies else "待确认"
        buyer_name = companies[1] if len(companies) > 1 else "待确认"
        seller_tax_no = tax_numbers[0] if tax_numbers else "待确认"
        buyer_tax_no = tax_numbers[1] if len(tax_numbers) > 1 else "待确认"

        tax_rate = self._first_match(text, [r"(\d{1,2}(?:\.\d+)?)%"], "待确认")
        if tax_rate != "待确认":
            tax_rate = f"{tax_rate}%"

        return {
            "invoice_no": invoice_no,
            "invoice_code": invoice_no,
            "invoice_date": invoice_date,
            "buyer_name": buyer_name,
            "buyer_tax_no": buyer_tax_no,
            "seller_name": seller_name,
            "seller_tax_no": seller_tax_no,
            "amount": f"{total_amount:.2f}" if total_amount else "未识别",
            "line_amount": f"{line_amount:.2f}" if line_amount else "0.00",
            "tax_amount": f"{tax_amount:.2f}" if tax_amount else "0.00",
            "total_amount": f"{total_amount:.2f}" if total_amount else "0.00",
            "tax_rate": tax_rate,
            "order_no": order_no,
            "flight_info": flight,
        }

    def _first_match(self, text: str, patterns: list[str], default: str) -> str:
        """按顺序返回第一个正则捕获结果。"""

        for pattern in patterns:
            match = re.search(pattern, text)
            if match:
                return match.group(1).strip()
        return default

    def _normalize_invoice_date(self, value: str) -> str:
        """把中文日期转换为 yyyy-MM-dd。"""

        match = re.match(r"(\d{4})年(\d{1,2})月(\d{1,2})日", value)
        if not match:
            return value or "待确认"
        year, month, day = match.groups()
        return f"{year}-{int(month):02d}-{int(day):02d}"


enterprise_ai_service = EnterpriseAiService()
