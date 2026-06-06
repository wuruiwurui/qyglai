package com.qyglai.automation.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReconciliationAnalyzeResponse(String status, BigDecimal difference,
                                            List<String> risks, List<String> suggestions) {
}
