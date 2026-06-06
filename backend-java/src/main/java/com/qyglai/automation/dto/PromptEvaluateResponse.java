package com.qyglai.automation.dto;

import java.util.List;

public record PromptEvaluateResponse(double score, List<String> issues, List<String> suggestions) {
}
