package com.qyglai.automation.dto;

import java.util.List;

public record KnowledgeAnswer(
        String answer,
        double confidence,
        List<String> citations,
        boolean humanHandoffSuggested
) {
}

