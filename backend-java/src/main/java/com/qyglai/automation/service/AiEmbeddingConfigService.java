package com.qyglai.automation.service;

import com.qyglai.automation.dto.AiEmbeddingConfig;
import com.qyglai.automation.dto.AiEmbeddingConfigView;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Embedding数据库配置服务，配置由AI治理页面维护，不依赖应用配置文件。
 */
@Service
public class AiEmbeddingConfigService {

    private final JdbcTemplate jdbcTemplate;
    private final JavaAiModelConfigService modelConfigService;

    public AiEmbeddingConfigService(JdbcTemplate jdbcTemplate, JavaAiModelConfigService modelConfigService) {
        this.jdbcTemplate = jdbcTemplate;
        this.modelConfigService = modelConfigService;
    }

    /** 自动创建Embedding配置表，并迁移当前已验证的默认配置。 */
    @PostConstruct
    public void initialize() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS ai_embedding_config (
                    id BIGINT NOT NULL COMMENT '主键ID',
                    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用真实Embedding',
                    provider VARCHAR(64) NOT NULL COMMENT '模型供应商',
                    api_url VARCHAR(512) NOT NULL COMMENT 'Embedding完整调用地址',
                    api_key VARCHAR(1024) NULL COMMENT 'Embedding API密钥',
                    model VARCHAR(128) NOT NULL COMMENT 'Embedding模型或接入点ID',
                    dimension INT NOT NULL COMMENT '向量维度',
                    milvus_url VARCHAR(512) NOT NULL COMMENT 'Milvus REST地址',
                    collection_name VARCHAR(128) NOT NULL COMMENT 'Milvus集合名称',
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    PRIMARY KEY (id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Embedding与向量数据库配置表'
                """);
        if (count() == 0) {
            String apiKey = modelConfigService.getConfig().apiKey();
            jdbcTemplate.update("""
                    INSERT INTO ai_embedding_config
                    (id, enabled, provider, api_url, api_key, model, dimension, milvus_url, collection_name)
                    VALUES (1, 1, 'doubao', ?, ?, ?, 2048, ?, ?)
                    """, "https://ark.cn-beijing.volces.com/api/v3/embeddings/multimodal", apiKey,
                    "ep-20260615092553-lqvch", "http://localhost:19530", "qyglai_knowledge_chunks");
        }
    }

    /** 获取包含真实密钥的服务端运行配置。 */
    public AiEmbeddingConfig getConfig() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM ai_embedding_config WHERE id=1");
        if (rows.isEmpty()) throw new IllegalStateException("Embedding配置不存在");
        Map<String, Object> row = rows.getFirst();
        return new AiEmbeddingConfig(number(row, "enabled") == 1, text(row, "provider"), text(row, "api_url"),
                text(row, "api_key"), text(row, "model"), number(row, "dimension"), text(row, "milvus_url"),
                text(row, "collection_name"));
    }

    /** 查询页面脱敏配置。 */
    public AiEmbeddingConfigView view() {
        return view(getConfig());
    }

    /** 保存页面配置，API Key留空时保留原密钥。 */
    public AiEmbeddingConfigView save(AiEmbeddingConfig request) {
        AiEmbeddingConfig current = getConfig();
        String apiKey = request.apiKey() == null || request.apiKey().isBlank() ? current.apiKey() : request.apiKey().trim();
        jdbcTemplate.update("""
                UPDATE ai_embedding_config SET enabled=?, provider=?, api_url=?, api_key=?, model=?,
                dimension=?, milvus_url=?, collection_name=? WHERE id=1
                """, request.enabled() ? 1 : 0, required(request.provider(), "供应商"),
                required(request.apiUrl(), "Embedding API地址"), apiKey, required(request.model(), "Embedding接入点ID"),
                positive(request.dimension(), "向量维度"), required(request.milvusUrl(), "Milvus地址"),
                required(request.collection(), "Milvus集合名称"));
        return view();
    }

    private AiEmbeddingConfigView view(AiEmbeddingConfig config) {
        return new AiEmbeddingConfigView(config.enabled(), config.provider(), config.apiUrl(), mask(config.apiKey()),
                config.model(), config.dimension(), config.milvusUrl(), config.collection());
    }

    private int count() {
        Integer value = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ai_embedding_config", Integer.class);
        return value == null ? 0 : value;
    }

    private String required(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + "不能为空");
        return value.trim();
    }

    private int positive(int value, String label) {
        if (value <= 0) throw new IllegalArgumentException(label + "必须大于0");
        return value;
    }

    private String text(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private int number(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value instanceof Number number ? number.intValue() : 0;
    }

    private String mask(String key) {
        if (key == null || key.isBlank()) return null;
        if (key.length() <= 8) return "*".repeat(key.length());
        return key.substring(0, 4) + "*".repeat(key.length() - 8) + key.substring(key.length() - 4);
    }
}
