package com.qyglai.automation.controller;

import java.util.List;

import com.qyglai.automation.common.ApiResponse;
import com.qyglai.automation.dto.ExtractionResult;
import com.qyglai.automation.dto.TextProcessRequest;
import com.qyglai.automation.entity.ContractRecordEntity;
import com.qyglai.automation.service.AutomationWorkspaceService;
import jakarta.validation.Valid;
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
    public ApiResponse<ExtractionResult> extract(@Valid @RequestBody TextProcessRequest request) {
        return ApiResponse.ok(service.extractContract(request));
    }

    /**
     * 查询合同台账。
     *
     * @return 合同列表
     */
    @GetMapping
    public ApiResponse<List<ContractRecordEntity>> list() {
        return ApiResponse.ok(service.listContracts());
    }
}
