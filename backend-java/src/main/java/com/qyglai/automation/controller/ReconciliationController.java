package com.qyglai.automation.controller;

import java.util.List;

import com.qyglai.automation.common.ApiResponse;
import com.qyglai.automation.dto.SimpleCreateRequest;
import com.qyglai.automation.entity.ReconciliationBatchEntity;
import com.qyglai.automation.entity.ReconciliationItemEntity;
import com.qyglai.automation.security.JwtPrincipal;
import com.qyglai.automation.service.ReconciliationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 财务对账接口。
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/reconciliation")
public class ReconciliationController {

    private final ReconciliationService service;

    public ReconciliationController(ReconciliationService service) {
        this.service = service;
    }

    /**
     * 创建对账批次。
     *
     * @param request 创建请求
     * @return 对账批次
     */
    @PostMapping("/batches")
    public ApiResponse<ReconciliationBatchEntity> createBatch(@RequestBody SimpleCreateRequest request,
                                                              Authentication authentication) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(service.createBatch(request, principal.userId()));
    }

    /**
     * 查询对账批次。
     *
     * @return 批次列表
     */
    @GetMapping("/batches")
    public ApiResponse<List<ReconciliationBatchEntity>> batches(Authentication authentication) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(service.listBatches(principal.userId(), principal.permissions().contains("*")));
    }

    /**
     * 查询对账明细。
     *
     * @return 明细列表
     */
    @GetMapping("/items")
    public ApiResponse<List<ReconciliationItemEntity>> items(Authentication authentication) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(service.listItems(principal.userId(), principal.permissions().contains("*")));
    }
}
