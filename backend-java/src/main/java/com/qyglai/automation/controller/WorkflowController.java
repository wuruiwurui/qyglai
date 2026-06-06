package com.qyglai.automation.controller;

import java.util.List;

import com.qyglai.automation.common.ApiResponse;
import com.qyglai.automation.dto.WorkflowActionRequest;
import com.qyglai.automation.dto.WorkflowDefinitionSaveRequest;
import com.qyglai.automation.dto.WorkflowInstanceDetail;
import com.qyglai.automation.entity.WorkflowDefinitionEntity;
import com.qyglai.automation.entity.WorkflowInstanceEntity;
import com.qyglai.automation.entity.WorkflowTaskEntity;
import com.qyglai.automation.dto.WorkflowInstanceSummary;
import com.qyglai.automation.dto.WorkflowStartRequest;
import com.qyglai.automation.security.JwtPrincipal;
import com.qyglai.automation.service.WorkflowApprovalService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/workflows")
@PreAuthorize("hasAnyAuthority('*', 'workflow:view')")
public class WorkflowController {

    private final WorkflowApprovalService service;

    public WorkflowController(WorkflowApprovalService service) {
        this.service = service;
    }

    @PostMapping("/start")
    public ApiResponse<WorkflowInstanceSummary> start(@Valid @RequestBody WorkflowStartRequest request, Authentication authentication) {
        return ApiResponse.ok(service.start(request, principal(authentication).userId()));
    }

    /** 查询当前用户的审批待办。 */
    @GetMapping("/tasks")
    public ApiResponse<List<WorkflowTaskEntity>> tasks(Authentication authentication) {
        JwtPrincipal principal = principal(authentication);
        return ApiResponse.ok(service.listPendingTasks(principal.userId(), principal.permissions().contains("*")));
    }

    /** 查询全部流程定义。 */
    @GetMapping("/definitions")
    public ApiResponse<List<WorkflowDefinitionEntity>> definitions() {
        return ApiResponse.ok(service.listDefinitions());
    }

    /** 保存新版本流程定义。 */
    @PostMapping("/definitions")
    @PreAuthorize("hasAnyAuthority('*', 'system:manage')")
    public ApiResponse<WorkflowDefinitionEntity> saveDefinition(@Valid @RequestBody WorkflowDefinitionSaveRequest request) {
        return ApiResponse.ok(service.saveDefinition(request));
    }

    /** 查询全部流程实例。 */
    @GetMapping("/instances")
    public ApiResponse<List<WorkflowInstanceEntity>> instances() {
        return ApiResponse.ok(service.listInstances());
    }

    /** 查询流程实例详情和审批历史。 */
    @GetMapping("/instances/{instanceId}")
    public ApiResponse<WorkflowInstanceDetail> detail(@PathVariable Long instanceId) {
        return ApiResponse.ok(service.detail(instanceId));
    }

    /** 处理审批任务。 */
    @PostMapping("/tasks/{taskId}/actions")
    public ApiResponse<WorkflowInstanceDetail> action(@PathVariable Long taskId,
                                                      @Valid @RequestBody WorkflowActionRequest request,
                                                      Authentication authentication) {
        JwtPrincipal principal = principal(authentication);
        return ApiResponse.ok(service.act(taskId, request, principal.userId(), principal.permissions().contains("*")));
    }

    private JwtPrincipal principal(Authentication authentication) {
        return (JwtPrincipal) authentication.getPrincipal();
    }
}
