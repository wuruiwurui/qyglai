"""老板助手模型兼容导出。

历史代码可能从 `app.schemas.boss` 导入模型，因此这里保留兼容入口。
"""

from app.schemas.ai import BossQueryRequest, BossQueryResponse, MetricCard

__all__ = ["BossQueryRequest", "BossQueryResponse", "MetricCard"]
