package com.qyglai.automation.dto;

import java.util.List;

public record ReportSummary(
        String title,
        String summary,
        List<String> sections,
        List<String> sources,
        String status
) {
}

