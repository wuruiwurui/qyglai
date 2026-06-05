"""AI 任务模型兼容导出。

历史代码可能从 `app.schemas.tasks` 导入模型，因此这里保留兼容入口。
"""

from app.schemas.ai import ClassificationResponse, ExtractionResponse, ReportResponse, TextTaskRequest

__all__ = ["ClassificationResponse", "ExtractionResponse", "ReportResponse", "TextTaskRequest"]
