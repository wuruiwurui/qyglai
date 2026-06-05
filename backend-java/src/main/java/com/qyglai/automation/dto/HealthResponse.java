package com.qyglai.automation.dto;

import java.time.Instant;

public record HealthResponse(String service, String status, Instant time) {
}

