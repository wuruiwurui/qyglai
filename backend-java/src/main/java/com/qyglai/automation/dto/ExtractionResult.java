package com.qyglai.automation.dto;

import java.util.List;
import java.util.Map;

public record ExtractionResult(
        String scenario,
        double confidence,
        Map<String, String> fields,
        List<String> risks,
        boolean reviewRequired
) {
}

