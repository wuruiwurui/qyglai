package com.qyglai.automation.controller;

import java.util.List;

import com.qyglai.automation.common.ApiResponse;
import com.qyglai.automation.dto.ReportGenerateRequest;
import com.qyglai.automation.dto.ReportSummary;
import com.qyglai.automation.entity.ReportRecordEntity;
import com.qyglai.automation.security.JwtPrincipal;
import com.qyglai.automation.service.AutomationWorkspaceService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final AutomationWorkspaceService service;

    public ReportController(AutomationWorkspaceService service) {
        this.service = service;
    }

    @PostMapping("/generate")
    public ApiResponse<ReportSummary> generate(@Valid @RequestBody ReportGenerateRequest request,
                                               Authentication authentication) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(service.generateReport(request, principal.userId()));
    }

    /**
     * 查询报表记录。
     *
     * @return 报表列表
     */
    @GetMapping
    public ApiResponse<List<ReportRecordEntity>> list(Authentication authentication) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(service.listReports(principal.userId(), principal.permissions().contains("*")));
    }
}
