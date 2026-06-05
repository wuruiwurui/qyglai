"""文档任务服务兼容入口。"""

from app.services.ai_service import enterprise_ai_service

# 历史路由使用的变量名，保留以避免旧代码导入失败。
ai_task_service = enterprise_ai_service
