package com.qyglai.automation.dto;

import java.math.BigDecimal;

public record SalesFollowupAdviceRequest(String customerName, String opportunityStage,
                                         int lastContactDays, BigDecimal amount) {
}
