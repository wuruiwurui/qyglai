package com.qyglai.automation.dto;

import java.util.List;

public record ReviewAdviceResponse(String decision, List<String> reasons,
                                   List<String> checklist, double confidence) {
}
