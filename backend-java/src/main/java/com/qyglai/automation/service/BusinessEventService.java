package com.qyglai.automation.service;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qyglai.automation.config.AutomationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * 业务事件发布服务，统一封装 Kafka 事件投递。
 */
@Service
public class BusinessEventService {

    private static final Logger log = LoggerFactory.getLogger(BusinessEventService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AutomationProperties properties;
    private final ObjectMapper objectMapper;

    public BusinessEventService(KafkaTemplate<String, String> kafkaTemplate,
                                AutomationProperties properties,
                                ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * 发布业务事件。Kafka 未启用时只记录日志，避免开发环境依赖 Kafka。
     *
     * @param eventType 事件类型
     * @param businessId 业务ID
     * @param payload 事件载荷
     */
    public void publish(String eventType, Long businessId, Map<String, Object> payload) {
        Map<String, Object> event = Map.of(
                "eventType", eventType,
                "businessId", businessId == null ? "" : businessId,
                "payload", payload,
                "time", Instant.now().toString()
        );
        if (!properties.getMessaging().isKafkaEnabled()) {
            log.info("Kafka disabled, event skipped: {}", event);
            return;
        }
        try {
            kafkaTemplate.send(properties.getMessaging().getDefaultTopic(), eventType, objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize business event", ex);
        }
    }
}

