package com.qyglai.automation.controller;

import java.util.List;

import com.qyglai.automation.common.ApiResponse;
import com.qyglai.automation.dto.ExtractionResult;
import com.qyglai.automation.dto.TextProcessRequest;
import com.qyglai.automation.entity.ContractRecordEntity;
import com.qyglai.automation.security.JwtPrincipal;
import com.qyglai.automation.service.AutomationWorkspaceService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    private final AutomationWorkspaceService service;

    public ContractController(AutomationWorkspaceService service) {
        this.service = service;
    }

    @PostMapping("/extract")
    public ApiResponse<ExtractionResult> extract(@Valid @RequestBody TextProcessRequest request,
                                                 Authentication authentication) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(service.extractContract(request, principal.userId()));
    }

    /**
     * 查询合同台账。
     *
     * @return 合同列表
     */
    @GetMapping
    public ApiResponse<List<ContractRecordEntity>> list(Authentication authentication) {
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        return ApiResponse.ok(service.listContracts(principal.userId(), principal.permissions().contains("*")));
    }
}
