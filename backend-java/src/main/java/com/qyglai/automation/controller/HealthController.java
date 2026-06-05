package com.qyglai.automation.controller;

import java.time.Instant;

import com.qyglai.automation.dto.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public HealthResponse health() {
        return new HealthResponse("automation-backend", "UP", Instant.now());
    }
}

