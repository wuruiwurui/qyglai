package com.qyglai.automation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 企业自动化平台的基础设施开关配置。
 */
@ConfigurationProperties(prefix = "automation")
@Component
public class AutomationProperties {

    /**
     * 消息配置。
     */
    private Messaging messaging = new Messaging();

    /**
     * 缓存配置。
     */
    private Cache cache = new Cache();


    public Messaging getMessaging() {
        return messaging;
    }

    public void setMessaging(Messaging messaging) {
        this.messaging = messaging;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }


    /**
     * Kafka 消息配置。
     */
    public static class Messaging {
        /**
         * 是否启用 Kafka 事件发布。
         */
        private boolean kafkaEnabled;

        /**
         * 默认业务事件 Topic。
         */
        private String defaultTopic = "qyglai-business-events";

        public boolean isKafkaEnabled() {
            return kafkaEnabled;
        }

        public void setKafkaEnabled(boolean kafkaEnabled) {
            this.kafkaEnabled = kafkaEnabled;
        }

        public String getDefaultTopic() {
            return defaultTopic;
        }

        public void setDefaultTopic(String defaultTopic) {
            this.defaultTopic = defaultTopic;
        }
    }

    /**
     * Redis 缓存配置。
     */
    public static class Cache {
        /**
         * 是否启用 Redis 缓存读写。
         */
        private boolean redisEnabled;

        public boolean isRedisEnabled() {
            return redisEnabled;
        }

        public void setRedisEnabled(boolean redisEnabled) {
            this.redisEnabled = redisEnabled;
        }
    }

}
