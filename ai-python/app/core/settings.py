"""应用配置模块。

该模块集中管理 AI 服务运行参数，避免端口、跨域、模型供应商等配置散落在路由或服务代码里。
"""

from functools import lru_cache
from typing import Literal

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """AI 服务配置对象。

    字段均支持通过环境变量覆盖，例如 `AI_PROVIDER=openai`。
    """

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    app_name: str = Field(default="QYGL AI Service", description="应用名称。")
    environment: Literal["local", "dev", "test", "prod"] = Field(default="local", description="当前运行环境。")
    api_prefix: str = Field(default="/api/v1", description="API 前缀。")
    cors_origins: list[str] = Field(default_factory=lambda: ["*"], description="跨域白名单。")
    default_model: str = Field(default="mock-local-model", description="默认模型名称。")
    ai_provider: Literal["mock", "openai", "local"] = Field(default="mock", description="模型供应商。")
    model_api_base: str | None = Field(default=None, description="OpenAI兼容模型服务地址。")
    model_api_key: str | None = Field(default=None, description="模型服务访问密钥。")
    request_timeout_seconds: int = Field(default=15, description="单次 AI 请求超时时间，单位秒。")
    human_review_threshold: float = Field(default=0.82, description="低置信度时是否建议人工复核。")
    log_level: str = Field(default="INFO", description="日志级别。")


@lru_cache(maxsize=1)
def get_settings() -> Settings:
    """读取并缓存配置。"""

    return Settings()


settings = get_settings()
