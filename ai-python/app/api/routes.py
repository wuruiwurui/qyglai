"""顶层 API 路由。"""

from fastapi import APIRouter

from app.core.settings import settings
from app.schemas.common import HealthResponse

router = APIRouter(tags=["health"])


@router.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    """AI 服务健康检查。"""

    return HealthResponse(
        service="ai-python",
        status="UP",
        model=settings.default_model,
        environment=settings.environment,
    )
