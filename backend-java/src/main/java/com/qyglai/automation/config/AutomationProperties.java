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

    /**
     * 知识库真实向量配置。
     */
    private VectorStore vectorStore = new VectorStore();

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

    public VectorStore getVectorStore() {
        return vectorStore;
    }

    public void setVectorStore(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
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

    /**
     * 豆包Embedding与Milvus向量库配置。
     */
    public static class VectorStore {
        /** 是否启用真实向量检索。 */
        private boolean enabled = true;
        /** 豆包Embedding接入点ID。 */
        private String embeddingModel = "ep-20260615092553-lqvch";
        /** 豆包Embedding接口地址。 */
        private String embeddingUrl = "https://ark.cn-beijing.volces.com/api/v3/embeddings/multimodal";
        /** Embedding输出维度。 */
        private int dimension = 2048;
        /** Milvus REST地址。 */
        private String milvusUrl = "http://localhost:19530";
        /** Milvus知识切片集合名称。 */
        private String collection = "qyglai_knowledge_chunks";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getEmbeddingModel() { return embeddingModel; }
        public void setEmbeddingModel(String embeddingModel) { this.embeddingModel = embeddingModel; }
        public String getEmbeddingUrl() { return embeddingUrl; }
        public void setEmbeddingUrl(String embeddingUrl) { this.embeddingUrl = embeddingUrl; }
        public int getDimension() { return dimension; }
        public void setDimension(int dimension) { this.dimension = dimension; }
        public String getMilvusUrl() { return milvusUrl; }
        public void setMilvusUrl(String milvusUrl) { this.milvusUrl = milvusUrl; }
        public String getCollection() { return collection; }
        public void setCollection(String collection) { this.collection = collection; }
    }
}
