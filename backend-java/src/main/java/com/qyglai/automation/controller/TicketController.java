package com.qyglai.automation.controller;

import java.util.List;

import com.qyglai.automation.common.ApiResponse;
import com.qyglai.automation.dto.TextProcessRequest;
import com.qyglai.automation.dto.TicketClassifyResult;
import com.qyglai.automation.entity.TicketEntity;
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
@RequestMapping("/api/tickets")
public class TicketController {

    private final AutomationWorkspaceService service;

    public TicketController(AutomationWorkspaceService service) {
        this.service = service;
    }

    @PostMapping("/classify")
    public ApiResponse<TicketClassifyResult> classify(@Valid @RequestBody TextProcessRequest request) {
        return ApiResponse.ok(service.classifyTicket(request));
    }

    /**
     * 查询客服工单。
     *
     * @return 工单列表
     */
    @GetMapping
    public ApiResponse<List<TicketEntity>> list() {
        return ApiResponse.ok(service.listTickets());
    }
}
