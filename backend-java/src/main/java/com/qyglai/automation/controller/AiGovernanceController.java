package com.qyglai.automation.controller;

import java.util.List;

import com.qyglai.automation.common.ApiResponse;
import com.qyglai.automation.dto.SimpleCreateRequest;
import com.qyglai.automation.entity.AiEvaluationSampleEntity;
import com.qyglai.automation.entity.AiModelCallLogEntity;
import com.qyglai.automation.entity.AiModelProviderEntity;
import com.qyglai.automation.entity.AiPromptTemplateEntity;
import com.qyglai.automation.service.AiGovernanceService;
import com.qyglai.automation.service.AutomationWorkspaceService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 治理接口。
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/ai-governance")
public class AiGovernanceController {

    private final AutomationWorkspaceService service;
    private final AiGovernanceService aiGovernanceService;

    public AiGovernanceController(AutomationWorkspaceService service, AiGovernanceService aiGovernanceService) {
        this.service = service;
        this.aiGovernanceService = aiGovernanceService;
    }

    /**
     * 查询 AI 模型调用日志。
     *
     * @return AI模型调用日志列表
     */
    @GetMapping("/model-call-logs")
    public ApiResponse<List<AiModelCallLogEntity>> modelCallLogs() {
        return ApiResponse.ok(service.listAiModelCallLogs());
    }

    /**
     * 查询模型供应商。
     *
     * @return 模型供应商列表
     */
    @GetMapping("/providers")
    public ApiResponse<List<AiModelProviderEntity>> providers() {
        return ApiResponse.ok(aiGovernanceService.listProviders());
    }

    /**
     * 创建提示词模板。
     *
     * @param request 创建请求
     * @return 提示词模板
     */
    @PostMapping("/prompt-templates")
    public ApiResponse<AiPromptTemplateEntity> createPromptTemplate(@RequestBody SimpleCreateRequest request) {
        return ApiResponse.ok(aiGovernanceService.createPromptTemplate(request));
    }

    /**
     * 查询提示词模板。
     *
     * @return 提示词模板列表
     */
    @GetMapping("/prompt-templates")
    public ApiResponse<List<AiPromptTemplateEntity>> promptTemplates() {
        return ApiResponse.ok(aiGovernanceService.listPromptTemplates());
    }

    /**
     * 查询评测样本。
     *
     * @return 评测样本列表
     */
    @GetMapping("/evaluation-samples")
    public ApiResponse<List<AiEvaluationSampleEntity>> evaluationSamples() {
        return ApiResponse.ok(aiGovernanceService.listEvaluationSamples());
    }
}
