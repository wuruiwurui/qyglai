package com.qyglai.automation.controller;

import java.util.List;

import com.qyglai.automation.common.ApiResponse;
import com.qyglai.automation.dto.SalesFollowupTask;
import com.qyglai.automation.service.AutomationWorkspaceService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/sales")
public class SalesController {

    private final AutomationWorkspaceService service;

    public SalesController(AutomationWorkspaceService service) {
        this.service = service;
    }

    @GetMapping("/followups")
    public ApiResponse<List<SalesFollowupTask>> followups() {
        return ApiResponse.ok(service.listSalesFollowups());
    }
}

