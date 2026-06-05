"""通用数据模型。"""

from datetime import datetime
from typing import Generic, TypeVar

from pydantic import BaseModel, Field

T = TypeVar("T")


class ApiResponse(BaseModel, Generic[T]):
    """统一响应模型。"""

    code: str = Field(default="OK", description="业务编码。")
    message: str = Field(default="success", description="响应说明。")
    data: T = Field(description="响应数据。")
    time: datetime = Field(default_factory=datetime.utcnow, description="响应时间。")


class HealthResponse(BaseModel):
    """健康检查响应。"""

    service: str = Field(description="服务名称。")
    status: str = Field(description="服务状态。")
    model: str = Field(description="当前默认模型。")
    environment: str = Field(description="运行环境。")
    time: datetime = Field(default_factory=datetime.utcnow, description="服务时间。")
