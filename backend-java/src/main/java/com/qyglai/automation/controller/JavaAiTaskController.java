package com.qyglai.automation.controller;

import com.qyglai.automation.common.ApiResponse;
import com.qyglai.automation.dto.PromptEvaluateRequest;
import com.qyglai.automation.dto.PromptEvaluateResponse;
import com.qyglai.automation.dto.ReconciliationAnalyzeRequest;
import com.qyglai.automation.dto.ReconciliationAnalyzeResponse;
import com.qyglai.automation.dto.ReviewAdviceRequest;
import com.qyglai.automation.dto.ReviewAdviceResponse;
import com.qyglai.automation.dto.SalesFollowupAdviceRequest;
import com.qyglai.automation.dto.SalesFollowupAdviceResponse;
import com.qyglai.automation.service.JavaAiTaskService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-tasks")
public class JavaAiTaskController {

    private final JavaAiTaskService service;

    public JavaAiTaskController(JavaAiTaskService service) {
        this.service = service;
    }

    @PostMapping("/sales/followup-reminder")
    public ApiResponse<SalesFollowupAdviceResponse> salesFollowup(@RequestBody SalesFollowupAdviceRequest request) {
        return ApiResponse.ok(service.salesFollowup(request));
    }

    @PostMapping("/reconciliation/analyze")
    public ApiResponse<ReconciliationAnalyzeResponse> reconciliation(@RequestBody ReconciliationAnalyzeRequest request) {
        return ApiResponse.ok(service.reconciliation(request));
    }

    @PostMapping("/review/advice")
    public ApiResponse<ReviewAdviceResponse> review(@RequestBody ReviewAdviceRequest request) {
        return ApiResponse.ok(service.review(request));
    }

    @PostMapping("/prompts/evaluate")
    public ApiResponse<PromptEvaluateResponse> evaluatePrompt(@RequestBody PromptEvaluateRequest request) {
        return ApiResponse.ok(service.evaluatePrompt(request));
    }
}
