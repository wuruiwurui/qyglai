"""老板助手路由。"""

from fastapi import APIRouter

from app.schemas.ai import BossQueryRequest, BossQueryResponse
from app.services.ai_service import enterprise_ai_service

router = APIRouter(prefix="/boss", tags=["boss"])


@router.post("/query", response_model=BossQueryResponse)
def boss_query(payload: BossQueryRequest) -> BossQueryResponse:
    """老板通过自然语言获取经营报表、异常和建议动作。"""

    return enterprise_ai_service.boss_query(payload)
