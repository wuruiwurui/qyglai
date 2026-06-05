# QYGL AI Python Service

企业流程自动化 AI 服务，负责文档抽取、发票解析、客服工单分类、知识库问答、老板经营助手、报表生成、复核建议、销售跟进提醒、对账分析和 Prompt 评测。

## 启动

```powershell
pip install -r requirements.txt
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000
```

## 核心接口

- `GET /health`
- `GET /api/v1/health`
- `POST /api/v1/boss/query`
- `POST /api/v1/documents/extract`
- `POST /api/v1/contracts/extract`
- `POST /api/v1/invoices/parse`
- `POST /api/v1/tickets/classify`
- `POST /api/v1/reports/generate`
- `POST /api/v1/kb/query`
- `POST /api/v1/review/advice`
- `POST /api/v1/sales/followup-reminder`
- `POST /api/v1/reconciliation/analyze`
- `POST /api/v1/prompts/evaluate`

当前实现使用本地规则模型，保证没有外部模型 Key 时也能稳定联调。后续接入真实模型时，可替换 `app/services/ai_service.py` 中的 provider 调用。
