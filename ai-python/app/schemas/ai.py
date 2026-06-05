"""AI 业务请求与响应模型。"""

from typing import Any

from pydantic import BaseModel, Field


class MetricCard(BaseModel):
    """经营指标卡片。"""

    name: str = Field(description="指标名称。")
    value: str = Field(description="指标值。")
    trend: str = Field(description="趋势说明。")
    status: str = Field(description="指标状态：normal、warning、danger。")


class BossQueryRequest(BaseModel):
    """老板助手查询请求。"""

    question: str = Field(min_length=1, description="老板提出的问题。")
    timeRange: str | None = Field(default=None, description="时间范围，例如 today、this_week、this_month。")
    userId: str | None = Field(default=None, description="用户ID。")


class BossQueryResponse(BaseModel):
    """老板助手查询响应。"""

    intent: str = Field(description="问题意图，例如 overview、sales、finance、customer_service、contract、review、report。")
    answer: str = Field(description="自然语言回答。")
    metrics: list[MetricCard] = Field(description="相关经营指标。")
    actions: list[str] = Field(description="建议动作。")
    sources: list[str] = Field(description="数据来源。")


class TextTaskRequest(BaseModel):
    """通用文本处理请求。"""

    content: str = Field(min_length=1, description="待处理文本。")
    scenario: str = Field(default="general", description="业务场景。")
    operatorId: str | None = Field(default=None, description="操作人ID。")


class ExtractionResponse(BaseModel):
    """文档抽取响应。"""

    scenario: str = Field(description="业务场景。")
    confidence: float = Field(description="模型置信度。")
    fields: dict[str, str] = Field(description="抽取出的结构化字段。")
    risks: list[str] = Field(description="识别到的风险点。")
    reviewRequired: bool = Field(description="是否需要人工复核。")


class ClassificationResponse(BaseModel):
    """客服工单分类响应。"""

    category: str = Field(description="工单分类。")
    priority: str = Field(description="优先级。")
    sentiment: str = Field(description="情绪倾向。")
    suggestedOwner: str = Field(description="建议处理人或团队。")
    confidence: float = Field(description="模型置信度。")
    suggestedReply: str = Field(description="建议回复。")


class ReportRequest(BaseModel):
    """报表生成请求。"""

    reportType: str = Field(default="daily", description="报表类型。")
    timeRange: str = Field(default="today", description="时间范围。")
    audience: str = Field(default="boss", description="接收对象。")
    autoSend: bool = Field(default=False, description="是否自动发送。")
    context: dict[str, Any] = Field(default_factory=dict, description="外部业务上下文。")


class ReportResponse(BaseModel):
    """报表生成响应。"""

    title: str = Field(description="报表标题。")
    summary: str = Field(description="报表摘要。")
    sections: list[str] = Field(description="报表章节。")
    sources: list[str] = Field(description="数据来源。")
    status: str = Field(description="报表状态。")


class KnowledgeQueryRequest(BaseModel):
    """知识库问答请求。"""

    question: str = Field(min_length=1, description="问题。")
    scope: str = Field(default="company", description="知识范围。")
    userId: str | None = Field(default=None, description="用户ID。")


class KnowledgeAnswer(BaseModel):
    """知识库问答响应。"""

    answer: str = Field(description="回答。")
    confidence: float = Field(description="置信度。")
    citations: list[str] = Field(description="引用来源。")
    humanHandoffSuggested: bool = Field(description="是否建议转人工。")


class ReviewAdviceRequest(BaseModel):
    """人工复核建议请求。"""

    scenario: str = Field(description="业务场景。")
    content: str = Field(min_length=1, description="待复核内容。")
    riskLevel: str | None = Field(default=None, description="已知风险等级。")


class ReviewAdviceResponse(BaseModel):
    """人工复核建议响应。"""

    decision: str = Field(description="建议结论。")
    reasons: list[str] = Field(description="建议原因。")
    checklist: list[str] = Field(description="复核清单。")
    confidence: float = Field(description="置信度。")


class SalesFollowupRequest(BaseModel):
    """销售跟进提醒请求。"""

    customerName: str = Field(description="客户名称。")
    opportunityStage: str = Field(description="商机阶段。")
    lastContactDays: int = Field(default=0, description="距上次联系天数。")
    amount: float | None = Field(default=None, description="商机金额。")


class SalesFollowupResponse(BaseModel):
    """销售跟进提醒响应。"""

    priority: str = Field(description="跟进优先级。")
    nextAction: str = Field(description="下一步动作。")
    dueHours: int = Field(description="建议多少小时内完成。")
    message: str = Field(description="提醒文案。")


class ReconciliationRequest(BaseModel):
    """对账分析请求。"""

    supplierName: str = Field(description="供应商名称。")
    statementAmount: float = Field(description="对账单金额。")
    invoiceAmount: float = Field(description="发票金额。")
    paidAmount: float = Field(default=0, description="已付款金额。")


class ReconciliationResponse(BaseModel):
    """对账分析响应。"""

    status: str = Field(description="对账状态。")
    difference: float = Field(description="差异金额。")
    risks: list[str] = Field(description="风险提示。")
    suggestions: list[str] = Field(description="处理建议。")


class PromptEvaluateRequest(BaseModel):
    """Prompt 评测请求。"""

    prompt: str = Field(min_length=1, description="待评测提示词。")
    scenario: str = Field(default="general", description="业务场景。")


class PromptEvaluateResponse(BaseModel):
    """Prompt 评测响应。"""

    score: float = Field(description="评分。")
    issues: list[str] = Field(description="问题列表。")
    suggestions: list[str] = Field(description="优化建议。")


class ModelRuntimeConfig(BaseModel):
    """页面维护的真实 AI 模型运行配置。"""

    provider: str = Field(default="mock", description="模型供应商，例如 mock、doubao、openai。")
    apiBase: str | None = Field(default=None, description="OpenAI兼容接口地址。")
    apiKey: str | None = Field(default=None, description="模型访问密钥。")
    model: str = Field(default="mock-local-model", description="模型名称或豆包 endpoint-id。")
    enabled: bool = Field(default=False, description="是否启用真实模型。")
    textGenerationEnabled: bool = Field(default=True, description="文本生成场景是否允许调用真实模型。")
    fileExtractionMode: str = Field(default="rules_first", description="文件抽取模式：rules_first、model_first、rules_only。")
    contractRiskMode: str = Field(default="rules_first", description="合同风险识别模式：rules_first、model_first、rules_only。")
    remark: str | None = Field(default=None, description="备注说明。")


class ModelRuntimeConfigView(BaseModel):
    """模型配置展示对象，密钥会脱敏。"""

    provider: str = Field(description="模型供应商。")
    apiBase: str | None = Field(description="接口地址。")
    apiKeyMasked: str | None = Field(description="脱敏后的访问密钥。")
    model: str = Field(description="模型名称。")
    enabled: bool = Field(description="是否启用。")
    textGenerationEnabled: bool = Field(description="文本生成是否允许调用真实模型。")
    fileExtractionMode: str = Field(description="文件抽取模式。")
    contractRiskMode: str = Field(description="合同风险识别模式。")
    remark: str | None = Field(description="备注说明。")


class ModelRuntimeStatus(BaseModel):
    """模型运行状态。"""

    provider: str = Field(description="模型供应商。")
    model: str = Field(description="模型名称。")
    enabled: bool = Field(description="真实模型是否启用。")
    apiBase: str | None = Field(description="接口地址。")
    textGenerationEnabled: bool = Field(description="文本生成是否允许调用真实模型。")
    fileExtractionMode: str = Field(description="文件抽取模式。")
    contractRiskMode: str = Field(description="合同风险识别模式。")
    lastCallStatus: str = Field(description="最近一次模型调用状态。")
    lastFallbackReason: str | None = Field(description="最近一次降级原因。")


class ParsedFileResponse(BaseModel):
    """文件解析响应。"""

    filename: str = Field(description="文件名。")
    contentType: str = Field(description="文件类型。")
    extension: str = Field(description="文件扩展名。")
    rawText: str = Field(description="解析出的原始文本。")
    charCount: int = Field(description="文本字符数。")
    warnings: list[str] = Field(default_factory=list, description="解析提示。")


class ParseAndExtractResponse(BaseModel):
    """文件解析并抽取响应。"""

    parsed: ParsedFileResponse = Field(description="文件解析结果。")
    extraction: ExtractionResponse = Field(description="AI抽取结果。")
