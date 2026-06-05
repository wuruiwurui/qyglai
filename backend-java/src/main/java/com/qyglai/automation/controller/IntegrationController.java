package com.qyglai.automation.controller;

import java.util.List;

import com.qyglai.automation.common.ApiResponse;
import com.qyglai.automation.dto.SimpleCreateRequest;
import com.qyglai.automation.entity.IntegrationConnectorEntity;
import com.qyglai.automation.entity.WebhookEventEntity;
import com.qyglai.automation.service.IntegrationService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 第三方集成接口。
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/integrations")
public class IntegrationController {

    private final IntegrationService service;

    public IntegrationController(IntegrationService service) {
        this.service = service;
    }

    /**
     * 创建连接器。
     *
     * @param request 创建请求
     * @return 连接器实体
     */
    @PostMapping("/connectors")
    public ApiResponse<IntegrationConnectorEntity> createConnector(@RequestBody SimpleCreateRequest request) {
        return ApiResponse.ok(service.createConnector(request));
    }

    /**
     * 查询连接器。
     *
     * @return 连接器列表
     */
    @GetMapping("/connectors")
    public ApiResponse<List<IntegrationConnectorEntity>> connectors() {
        return ApiResponse.ok(service.listConnectors());
    }

    /**
     * 接收 Webhook 事件。
     *
     * @param connectorId 连接器ID
     * @param eventType 事件类型
     * @param payloadJson 事件载荷
     * @return Webhook事件
     */
    @PostMapping("/webhooks")
    public ApiResponse<WebhookEventEntity> receiveWebhook(@RequestParam(required = false) Long connectorId,
                                                          @RequestParam String eventType,
                                                          @RequestBody(required = false) String payloadJson) {
        return ApiResponse.ok(service.receiveWebhook(connectorId, eventType, payloadJson));
    }

    /**
     * 查询 Webhook 事件。
     *
     * @return Webhook事件列表
     */
    @GetMapping("/webhooks")
    public ApiResponse<List<WebhookEventEntity>> webhooks() {
        return ApiResponse.ok(service.listWebhookEvents());
    }
}

