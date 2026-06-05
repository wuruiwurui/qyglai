"""日志配置模块。"""

import logging
from logging.config import dictConfig

from app.core.settings import settings


def setup_logging() -> None:
    """初始化应用日志。

    使用标准库日志，避免引入额外依赖；生产环境后续可接入 ELK、Prometheus 或 OpenTelemetry。
    """

    dictConfig(
        {
            "version": 1,
            "disable_existing_loggers": False,
            "formatters": {
                "default": {
                    "format": "%(asctime)s %(levelname)s [%(name)s] %(message)s",
                }
            },
            "handlers": {
                "console": {
                    "class": "logging.StreamHandler",
                    "formatter": "default",
                }
            },
            "root": {
                "handlers": ["console"],
                "level": settings.log_level,
            },
        }
    )


logger = logging.getLogger("qyglai.ai")
