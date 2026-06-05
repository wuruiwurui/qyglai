"""本地规则引擎。

该模块提供可解释、可测试的基础 AI 能力模拟。
在企业项目早期，它可以保障接口稳定；后续接入真实大模型时，仍可作为兜底策略。
"""

import re
from dataclasses import dataclass


@dataclass(frozen=True)
class AmountMatch:
    """金额识别结果。"""

    raw: str
    value: float


class RuleEngine:
    """可复用文本规则引擎。"""

    amount_pattern = re.compile(r"(?P<amount>\d+(?:\.\d+)?)\s*(?P<unit>万|元)?")

    def normalize(self, text: str) -> str:
        """清理输入文本，降低后续规则处理复杂度。"""

        return " ".join(text.strip().split())

    def extract_amounts(self, text: str) -> list[AmountMatch]:
        """从文本中提取金额数字。"""

        amounts: list[AmountMatch] = []
        for match in self.amount_pattern.finditer(text):
            raw_amount = match.group("amount")
            unit = match.group("unit") or ""
            value = float(raw_amount)
            if unit == "万":
                value *= 10000
            amounts.append(AmountMatch(raw=f"{raw_amount}{unit}", value=value))
        return amounts

    def contains_any(self, text: str, keywords: list[str]) -> bool:
        """判断文本是否包含任一关键词。"""

        return any(keyword in text for keyword in keywords)

    def confidence(self, base: float, text: str) -> float:
        """根据文本长度给出稳定置信度。"""

        bonus = min(len(text) / 1000, 0.08)
        return round(min(base + bonus, 0.97), 2)


rule_engine = RuleEngine()
