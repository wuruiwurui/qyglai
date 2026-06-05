"""模型运行配置服务。"""

import json
from pathlib import Path

from app.core.settings import settings
from app.schemas.ai import ModelRuntimeConfig, ModelRuntimeConfigView


class ModelConfigService:
    """管理页面提交的模型配置。

    配置保存到本地 JSON 文件，适合当前单机开发和演示部署。
    生产环境可替换为数据库、配置中心或密钥管理服务。
    """

    def __init__(self) -> None:
        self.config_path = Path(__file__).resolve().parents[2] / "runtime" / "model_config.json"

    def get_config(self) -> ModelRuntimeConfig:
        """读取模型配置，文件不存在时回退到环境变量配置。"""

        if self.config_path.exists():
            return ModelRuntimeConfig.model_validate_json(self.config_path.read_text(encoding="utf-8"))
        return ModelRuntimeConfig(
            provider=settings.ai_provider,
            apiBase=settings.model_api_base,
            apiKey=settings.model_api_key,
            model=settings.default_model,
            enabled=settings.ai_provider != "mock",
            textGenerationEnabled=True,
            fileExtractionMode="rules_first",
            contractRiskMode="rules_first",
            remark="来自环境变量默认配置",
        )

    def save_config(self, config: ModelRuntimeConfig) -> ModelRuntimeConfigView:
        """保存模型配置。"""

        self.config_path.parent.mkdir(parents=True, exist_ok=True)
        self.config_path.write_text(config.model_dump_json(indent=2), encoding="utf-8")
        return self.to_view(config)

    def to_view(self, config: ModelRuntimeConfig | None = None) -> ModelRuntimeConfigView:
        """生成脱敏展示对象。"""

        current = config or self.get_config()
        return ModelRuntimeConfigView(
            provider=current.provider,
            apiBase=current.apiBase,
            apiKeyMasked=self.mask_key(current.apiKey),
            model=current.model,
            enabled=current.enabled,
            textGenerationEnabled=current.textGenerationEnabled,
            fileExtractionMode=current.fileExtractionMode,
            contractRiskMode=current.contractRiskMode,
            remark=current.remark,
        )

    def mask_key(self, api_key: str | None) -> str | None:
        """脱敏 API Key。"""

        if not api_key:
            return None
        if len(api_key) <= 8:
            return "*" * len(api_key)
        return f"{api_key[:4]}{'*' * (len(api_key) - 8)}{api_key[-4:]}"


model_config_service = ModelConfigService()
