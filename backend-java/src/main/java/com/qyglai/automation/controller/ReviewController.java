package com.qyglai.automation.controller;

import java.util.List;

import com.qyglai.automation.common.ApiResponse;
import com.qyglai.automation.dto.ReviewCompleteRequest;
import com.qyglai.automation.dto.ReviewTask;
import com.qyglai.automation.service.AutomationWorkspaceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final AutomationWorkspaceService service;

    public ReviewController(AutomationWorkspaceService service) {
        this.service = service;
    }

    @GetMapping("/tasks")
    public ApiResponse<List<ReviewTask>> tasks() {
        return ApiResponse.ok(service.listReviewTasks());
    }

    /**
     * 完成人工复核任务。
     *
     * @param id 复核任务ID
     * @param request 复核完成请求
     * @return 是否成功
     */
    @PutMapping("/tasks/{id}/complete")
    public ApiResponse<Boolean> complete(@PathVariable Long id, @Valid @RequestBody ReviewCompleteRequest request) {
        return ApiResponse.ok(service.completeReviewTask(id, request.result()));
    }
}
