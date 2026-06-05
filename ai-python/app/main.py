"""QYGL AI Python 服务入口。"""

from contextlib import asynccontextmanager
from collections.abc import AsyncIterator

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.routes import router as health_router
from app.api.routers.boss import router as boss_router
from app.api.routers.tasks import router as tasks_router
from app.core.exceptions import register_exception_handlers
from app.core.logging import logger, setup_logging
from app.core.settings import settings
from app.schemas.common import HealthResponse


@asynccontextmanager
async def lifespan(_: FastAPI) -> AsyncIterator[None]:
    """应用生命周期钩子。"""

    setup_logging()
    logger.info("starting %s in %s mode", settings.app_name, settings.environment)
    yield
    logger.info("stopping %s", settings.app_name)


def create_app() -> FastAPI:
    """创建 FastAPI 应用实例。

    使用工厂函数便于后续测试、灰度部署和多实例启动。
    """

    app = FastAPI(
        title=settings.app_name,
        description="企业流程自动化 AI 服务：文档抽取、发票解析、工单分类、知识库问答、经营报表、复核建议与治理评测。",
        version="1.0.0",
        lifespan=lifespan,
    )

    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.cors_origins,
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    register_exception_handlers(app)
    app.include_router(health_router, prefix=settings.api_prefix)
    app.include_router(boss_router, prefix=settings.api_prefix)
    app.include_router(tasks_router, prefix=settings.api_prefix)
    return app


app = create_app()


@app.get("/health", response_model=HealthResponse)
def root_health() -> HealthResponse:
    """根路径健康检查，供容器和网关探活使用。"""

    return HealthResponse(
        service="ai-python",
        status="UP",
        model=settings.default_model,
        environment=settings.environment,
    )
