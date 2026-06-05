package com.qyglai.automation.controller;

import java.util.List;

import com.qyglai.automation.common.ApiResponse;
import com.qyglai.automation.entity.WorkflowTaskEntity;
import com.qyglai.automation.dto.WorkflowInstanceSummary;
import com.qyglai.automation.dto.WorkflowStartRequest;
import com.qyglai.automation.service.AutomationWorkspaceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {

    private final AutomationWorkspaceService service;

    public WorkflowController(AutomationWorkspaceService service) {
        this.service = service;
    }

    @PostMapping("/start")
    public ApiResponse<WorkflowInstanceSummary> start(@Valid @RequestBody WorkflowStartRequest request) {
        return ApiResponse.ok(service.startWorkflow(request));
    }

    /**
     * 查询流程任务。
     *
     * @return 流程任务列表
     */
    @GetMapping("/tasks")
    public ApiResponse<List<WorkflowTaskEntity>> tasks() {
        return ApiResponse.ok(service.listWorkflowTasks());
    }
}
