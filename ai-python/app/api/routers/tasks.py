"""AI 任务路由。"""

from fastapi import APIRouter, File, Form, UploadFile

from app.schemas.ai import (
    ClassificationResponse,
    ExtractionResponse,
    KnowledgeAnswer,
    KnowledgeQueryRequest,
    ModelRuntimeConfig,
    ModelRuntimeConfigView,
    ModelRuntimeStatus,
    ParseAndExtractResponse,
    ParsedFileResponse,
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
from app.services.ai_service import enterprise_ai_service
from app.services.file_parser_service import file_parser_service
from app.services.model_config_service import model_config_service
from app.services.model_provider import model_provider

router = APIRouter(tags=["ai-tasks"])


@router.get("/model-config", response_model=ModelRuntimeConfigView)
def get_model_config() -> ModelRuntimeConfigView:
    """查询页面维护的真实 AI 模型配置，API Key 会脱敏。"""

    return model_config_service.to_view()


@router.get("/model-status", response_model=ModelRuntimeStatus)
def get_model_status() -> ModelRuntimeStatus:
    """查询真实模型启用状态、场景开关和最近一次调用状态。"""

    return ModelRuntimeStatus(**model_provider.runtime_status())


@router.post("/model-config", response_model=ModelRuntimeConfigView)
def save_model_config(payload: ModelRuntimeConfig) -> ModelRuntimeConfigView:
    """保存真实 AI 模型配置。"""

    return model_config_service.save_config(payload)


@router.post("/files/parse", response_model=ParsedFileResponse)
async def parse_file(file: UploadFile = File(...)) -> ParsedFileResponse:
    """解析上传文件并返回原始文本。"""

    content = await file.read()
    return await file_parser_service.parse_upload(file.filename or "upload.bin", file.content_type, content)


@router.post("/files/parse-and-extract", response_model=ParseAndExtractResponse)
async def parse_and_extract_file(file: UploadFile = File(...), scenario: str = Form(default="general")) -> ParseAndExtractResponse:
    """解析文件并立即执行 AI 字段抽取。"""

    content = await file.read()
    parsed = await file_parser_service.parse_upload(file.filename or "upload.bin", file.content_type, content)
    extraction = enterprise_ai_service.extract_document(TextTaskRequest(content=parsed.rawText, scenario=scenario, operatorId="file-upload"))
    return ParseAndExtractResponse(parsed=parsed, extraction=extraction)


@router.post("/documents/extract", response_model=ExtractionResponse)
def extract_document(payload: TextTaskRequest) -> ExtractionResponse:
    """抽取合同、发票、对账单等文档中的关键字段。"""

    return enterprise_ai_service.extract_document(payload)


@router.post("/contracts/extract", response_model=ExtractionResponse)
def extract_contract(payload: TextTaskRequest) -> ExtractionResponse:
    """合同专项抽取接口。"""

    payload.scenario = "contract"
    return enterprise_ai_service.extract_document(payload)


@router.post("/invoices/parse", response_model=ExtractionResponse)
def parse_invoice(payload: TextTaskRequest) -> ExtractionResponse:
    """发票专项解析接口。"""

    payload.scenario = "invoice"
    return enterprise_ai_service.extract_document(payload)


@router.post("/tickets/classify", response_model=ClassificationResponse)
def classify_ticket(payload: TextTaskRequest) -> ClassificationResponse:
    """客服工单分类、优先级判断和建议回复。"""

    return enterprise_ai_service.classify_ticket(payload)


@router.post("/reports/generate", response_model=ReportResponse)
def generate_report(payload: ReportRequest) -> ReportResponse:
    """生成日报、周报或老板经营摘要。"""

    return enterprise_ai_service.generate_report(payload)


@router.post("/kb/query", response_model=KnowledgeAnswer)
def query_knowledge_base(payload: KnowledgeQueryRequest) -> KnowledgeAnswer:
    """企业知识库问答。"""

    return enterprise_ai_service.knowledge_query(payload)


@router.post("/review/advice", response_model=ReviewAdviceResponse)
def review_advice(payload: ReviewAdviceRequest) -> ReviewAdviceResponse:
    """为人工复核任务生成处理建议。"""

    return enterprise_ai_service.review_advice(payload)


@router.post("/sales/followup-reminder", response_model=SalesFollowupResponse)
def sales_followup(payload: SalesFollowupRequest) -> SalesFollowupResponse:
    """生成销售跟进提醒。"""

    return enterprise_ai_service.sales_followup(payload)


@router.post("/reconciliation/analyze", response_model=ReconciliationResponse)
def analyze_reconciliation(payload: ReconciliationRequest) -> ReconciliationResponse:
    """分析供应商对账差异。"""

    return enterprise_ai_service.analyze_reconciliation(payload)


@router.post("/prompts/evaluate", response_model=PromptEvaluateResponse)
def evaluate_prompt(payload: PromptEvaluateRequest) -> PromptEvaluateResponse:
    """评测 Prompt 模板质量并给出优化建议。"""

    return enterprise_ai_service.evaluate_prompt(payload)
