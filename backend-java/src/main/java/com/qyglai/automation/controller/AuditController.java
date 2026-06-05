package com.qyglai.automation.controller;

import java.util.List;

import com.qyglai.automation.common.ApiResponse;
import com.qyglai.automation.entity.AuditLogEntity;
import com.qyglai.automation.service.AuditQueryService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 审计日志接口。
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditQueryService service;

    public AuditController(AuditQueryService service) {
        this.service = service;
    }

    /**
     * 查询审计日志。
     *
     * @return 审计日志列表
     */
    @GetMapping("/logs")
    public ApiResponse<List<AuditLogEntity>> logs() {
        return ApiResponse.ok(service.listLogs());
    }
}
