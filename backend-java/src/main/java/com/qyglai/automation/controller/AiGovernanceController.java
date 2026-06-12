package com.qyglai.automation.controller;

import java.util.List;

import com.qyglai.automation.common.ApiResponse;
import com.qyglai.automation.dto.SimpleCreateRequest;
import com.qyglai.automation.dto.AiRuntimeConfig;
import com.qyglai.automation.dto.AiRuntimeConfigView;
import com.qyglai.automation.dto.AiRuntimeStatus;
import com.qyglai.automation.dto.AiModelProfile;
import com.qyglai.automation.dto.AiModelProfileView;
import com.qyglai.automation.dto.AiEvaluationDashboard;
import com.qyglai.automation.dto.AiScenarioRoute;
import com.qyglai.automation.dto.AiScenarioRouteView;
import com.qyglai.automation.entity.AiEvaluationSampleEntity;
import com.qyglai.automation.entity.AiModelCallLogEntity;
import com.qyglai.automation.entity.AiModelProviderEntity;
import com.qyglai.automation.entity.AiPromptTemplateEntity;
import com.qyglai.automation.service.AiGovernanceService;
import com.qyglai.automation.service.AutomationWorkspaceService;
import com.qyglai.automation.service.JavaAiModelConfigService;
import com.qyglai.automation.service.JavaAiModelGateway;
import com.qyglai.automation.service.AiEvaluationService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final JavaAiModelConfigService modelConfigService;
    private final JavaAiModelGateway modelGateway;
    private final AiEvaluationService aiEvaluationService;

    public AiGovernanceController(AutomationWorkspaceService service, AiGovernanceService aiGovernanceService,
                                  JavaAiModelConfigService modelConfigService, JavaAiModelGateway modelGateway,
                                  AiEvaluationService aiEvaluationService) {
        this.service = service;
        this.aiGovernanceService = aiGovernanceService;
        this.modelConfigService = modelConfigService;
        this.modelGateway = modelGateway;
        this.aiEvaluationService = aiEvaluationService;
    }

    @GetMapping("/runtime-config")
    public ApiResponse<AiRuntimeConfigView> runtimeConfig() {
        return ApiResponse.ok(modelConfigService.view());
    }

    @PostMapping("/runtime-config")
    public ApiResponse<AiRuntimeConfigView> saveRuntimeConfig(@RequestBody AiRuntimeConfig request) {
        return ApiResponse.ok(modelConfigService.save(request));
    }

    @GetMapping("/runtime-status")
    public ApiResponse<AiRuntimeStatus> runtimeStatus() {
        return ApiResponse.ok(modelGateway.status());
    }

    /**
     * 查询全部可切换模型配置。
     */
    @GetMapping("/model-profiles")
    public ApiResponse<List<AiModelProfileView>> modelProfiles() {
        return ApiResponse.ok(modelConfigService.listProfiles());
    }

    /**
     * 新增或更新模型配置。
     */
    @PostMapping("/model-profiles")
    public ApiResponse<AiModelProfileView> saveModelProfile(@RequestBody AiModelProfile request) {
        return ApiResponse.ok(modelConfigService.saveProfile(request));
    }

    /**
     * 切换当前使用模型。
     */
    @PostMapping("/model-profiles/{id}/switch")
    public ApiResponse<AiModelProfileView> switchModelProfile(@PathVariable String id) {
        return ApiResponse.ok(modelConfigService.switchCurrent(id));
    }

    /**
     * 删除非当前模型配置。
     */
    @DeleteMapping("/model-profiles/{id}")
    public ApiResponse<Void> deleteModelProfile(@PathVariable String id) {
        modelConfigService.deleteProfile(id);
        return ApiResponse.ok(null);
    }

    /** 查询全部业务场景模型路由。 */
    @GetMapping("/scenario-routes")
    public ApiResponse<List<AiScenarioRouteView>> scenarioRoutes() {
        return ApiResponse.ok(modelConfigService.listScenarioRoutes());
    }

    /** 保存业务场景的主模型与备用模型顺序。 */
    @PostMapping("/scenario-routes")
    public ApiResponse<AiScenarioRouteView> saveScenarioRoute(@RequestBody AiScenarioRoute request) {
        return ApiResponse.ok(modelConfigService.saveScenarioRoute(request));
    }

    /** 删除业务场景路由，恢复使用当前默认模型。 */
    @DeleteMapping("/scenario-routes/{scenario}")
    public ApiResponse<Void> deleteScenarioRoute(@PathVariable String scenario) {
        modelConfigService.deleteScenarioRoute(scenario);
        return ApiResponse.ok(null);
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

    /** 查询 AI 效果评估中心实时指标。 */
    @GetMapping("/evaluation-dashboard")
    public ApiResponse<AiEvaluationDashboard> evaluationDashboard(@RequestParam(defaultValue = "30") int days) {
        return ApiResponse.ok(aiEvaluationService.dashboard(days));
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
