package com.qyglai.automation.controller;

import java.util.List;

import com.qyglai.automation.common.ApiResponse;
import com.qyglai.automation.dto.ExtractionResult;
import com.qyglai.automation.dto.TextProcessRequest;
import com.qyglai.automation.entity.InvoiceRecordEntity;
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
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final AutomationWorkspaceService service;

    public InvoiceController(AutomationWorkspaceService service) {
        this.service = service;
    }

    @PostMapping("/parse")
    public ApiResponse<ExtractionResult> parse(@Valid @RequestBody TextProcessRequest request) {
        return ApiResponse.ok(service.parseInvoice(request));
    }

    /**
     * 查询发票记录。
     *
     * @return 发票列表
     */
    @GetMapping
    public ApiResponse<List<InvoiceRecordEntity>> list() {
        return ApiResponse.ok(service.listInvoices());
    }
}
