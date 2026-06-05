"""模型供应商适配层。"""

import httpx

from app.core.logging import logger
from app.core.settings import settings
from app.services.model_config_service import model_config_service


class ModelProvider:
    """OpenAI-compatible 模型供应商。

    当 `AI_PROVIDER=openai` 且配置 `MODEL_API_BASE`、`MODEL_API_KEY` 后，
    服务会优先调用真实模型；调用失败时返回传入的兜底文本，保障业务流程不断。
    """

    def __init__(self) -> None:
        self.last_call_status = "not_called"
        self.last_fallback_reason: str | None = None

    def generate_text(self, system_prompt: str, user_prompt: str, fallback: str) -> str:
        """生成文本。"""

        config = model_config_service.get_config()
        api_base = config.apiBase or settings.model_api_base
        api_key = config.apiKey or settings.model_api_key
        model = config.model or settings.default_model
        enabled = config.enabled and config.textGenerationEnabled and config.provider != "mock"
        if not enabled or not api_base or not api_key:
            self.last_call_status = "fallback"
            self.last_fallback_reason = "真实模型未启用、文本生成开关关闭或缺少 API 配置"
            return fallback
        try:
            with httpx.Client(timeout=settings.request_timeout_seconds) as client:
                response = client.post(
                    f"{api_base.rstrip('/')}/chat/completions",
                    headers={"Authorization": f"Bearer {api_key}"},
                    json={
                        "model": model,
                        "messages": [
                            {"role": "system", "content": system_prompt},
                            {"role": "user", "content": user_prompt},
                        ],
                        "temperature": 0.2,
                    },
                )
                response.raise_for_status()
                data = response.json()
                self.last_call_status = "success"
                self.last_fallback_reason = None
                return data["choices"][0]["message"]["content"]
        except Exception as exc:
            logger.warning("model provider fallback: %s", exc)
            self.last_call_status = "fallback"
            self.last_fallback_reason = str(exc)
            return fallback

    def runtime_status(self) -> dict:
        """返回当前模型运行状态。"""

        config = model_config_service.get_config()
        return {
            "provider": config.provider,
            "model": config.model,
            "enabled": config.enabled and config.provider != "mock",
            "apiBase": config.apiBase,
            "textGenerationEnabled": config.textGenerationEnabled,
            "fileExtractionMode": config.fileExtractionMode,
            "contractRiskMode": config.contractRiskMode,
            "lastCallStatus": self.last_call_status,
            "lastFallbackReason": self.last_fallback_reason,
        }


model_provider = ModelProvider()
