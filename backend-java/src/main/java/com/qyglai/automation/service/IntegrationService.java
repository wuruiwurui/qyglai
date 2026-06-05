package com.qyglai.automation.service;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qyglai.automation.dto.SimpleCreateRequest;
import com.qyglai.automation.entity.IntegrationConnectorEntity;
import com.qyglai.automation.entity.WebhookEventEntity;
import com.qyglai.automation.mapper.IntegrationConnectorMapper;
import com.qyglai.automation.mapper.WebhookEventMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 第三方集成服务，负责连接器与 Webhook 事件管理。
 */
@Service
public class IntegrationService {

    private final IntegrationConnectorMapper connectorMapper;
    private final WebhookEventMapper webhookEventMapper;
    private final AuditService auditService;

    public IntegrationService(IntegrationConnectorMapper connectorMapper, WebhookEventMapper webhookEventMapper, AuditService auditService) {
        this.connectorMapper = connectorMapper;
        this.webhookEventMapper = webhookEventMapper;
        this.auditService = auditService;
    }

    /**
     * 创建第三方连接器。
     *
     * @param request 创建请求
     * @return 连接器实体
     */
    public IntegrationConnectorEntity createConnector(SimpleCreateRequest request) {
        IntegrationConnectorEntity connector = new IntegrationConnectorEntity();
        connector.setConnectorCode(request.code() == null ? "CONN-" + System.currentTimeMillis() : request.code());
        connector.setConnectorName(request.name() == null ? "未命名连接器" : request.name());
        connector.setConnectorType(request.type() == null ? "webhook" : request.type());
        connector.setEndpointUrl(request.content());
        connector.setStatus("enabled");
        connectorMapper.insert(connector);
        auditService.record("CONNECTOR_CREATE", "创建集成连接器", "integration_connector", connector.getId());
        return connector;
    }

    /**
     * 查询第三方连接器。
     *
     * @return 连接器列表
     */
    public List<IntegrationConnectorEntity> listConnectors() {
        return connectorMapper.selectList(new LambdaQueryWrapper<IntegrationConnectorEntity>().orderByDesc(IntegrationConnectorEntity::getCreatedAt));
    }

    /**
     * 接收并登记 Webhook 事件。
     *
     * @param connectorId 连接器ID
     * @param eventType 事件类型
     * @param payloadJson 事件载荷
     * @return Webhook 事件实体
     */
    @Transactional(rollbackFor = Exception.class)
    public WebhookEventEntity receiveWebhook(Long connectorId, String eventType, String payloadJson) {
        WebhookEventEntity event = new WebhookEventEntity();
        event.setConnectorId(connectorId);
        event.setEventType(eventType);
        event.setEventKey(eventType + "-" + System.currentTimeMillis());
        event.setPayloadJson(payloadJson == null ? "{}" : payloadJson);
        event.setProcessStatus("pending");
        event.setReceivedAt(LocalDateTime.now());
        webhookEventMapper.insert(event);
        auditService.record("WEBHOOK_RECEIVE", "接收Webhook事件", "webhook_event", event.getId());
        return event;
    }

    /**
     * 查询 Webhook 事件。
     *
     * @return Webhook 事件列表
     */
    public List<WebhookEventEntity> listWebhookEvents() {
        return webhookEventMapper.selectList(new LambdaQueryWrapper<WebhookEventEntity>().orderByDesc(WebhookEventEntity::getReceivedAt));
    }
}

