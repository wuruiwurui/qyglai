package com.qyglai.automation.dto;

import java.math.BigDecimal;

public record ReconciliationAnalyzeRequest(String supplierName, BigDecimal statementAmount,
                                           BigDecimal invoiceAmount, BigDecimal paidAmount) {
}
