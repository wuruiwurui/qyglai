package com.qyglai.automation.controller;

import com.qyglai.automation.dto.BossChatRequest;
import com.qyglai.automation.dto.BossChatResponse;
import com.qyglai.automation.service.AiGatewayService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/boss-assistant")
public class BossAssistantController {

    private final AiGatewayService aiGatewayService;

    public BossAssistantController(AiGatewayService aiGatewayService) {
        this.aiGatewayService = aiGatewayService;
    }

    @PostMapping("/ask")
    public BossChatResponse ask(@Valid @RequestBody BossChatRequest request) {
        return aiGatewayService.askBossAssistant(request);
    }
}

