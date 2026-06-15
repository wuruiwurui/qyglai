package com.qyglai.automation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.qyglai.automation.dto.AiModelHealthStatus;
import com.qyglai.automation.dto.AiModelProfileView;
import com.qyglai.automation.dto.AiRuntimeConfig;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * AI模型健康检查服务，负责主动探测模型连接并为自动路由提供健康排序依据。
 */
@Service
public class AiModelHealthService {

    private static final Duration HEALTH_TTL = Duration.ofMinutes(5);
    private final JavaAiModelConfigService configService;
    private final Map<String, AiModelHealthStatus> statuses = new ConcurrentHashMap<>();

    public AiModelHealthService(JavaAiModelConfigService configService) {
        this.configService = configService;
    }

    /** 查询所有模型最近一次健康状态，尚未探测的模型返回 unknown。 */
    public List<AiModelHealthStatus> list() {
        return configService.listProfiles().stream().map(profile -> statuses.getOrDefault(profile.id(),
                new AiModelHealthStatus(profile.id(), profile.name(), profile.model(), "unknown", 0,
                        "尚未执行健康检查", null))).toList();
    }

    /** 对指定模型执行一次最小真实请求健康探测。 */
    public AiModelHealthStatus check(String profileId) {
        AiModelProfileView profile = configService.listProfiles().stream()
                .filter(item -> item.id().equals(profileId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("模型配置不存在"));
        AiRuntimeConfig config = configService.getConfig(profileId);
        long startedAt = System.nanoTime();
        AiModelHealthStatus result;
        try {
            validate(config);
            JsonNode response = client(config).post().uri("/chat/completions")
                    .body(Map.of(
                            "model", config.model(),
                            "messages", List.of(Map.of("role", "user", "content", "回复OK")),
                            "temperature", 0,
                            "max_tokens", 4))
                    .retrieve().body(JsonNode.class);
            String content = response == null ? null : response.at("/choices/0/message/content").asText(null);
            if (content == null || content.isBlank()) throw new IllegalStateException("模型未返回有效内容");
            result = status(profile, "healthy", elapsed(startedAt), "连接正常");
        } catch (Exception exception) {
            result = status(profile, "unhealthy", elapsed(startedAt), shortMessage(exception));
        }
        statuses.put(profileId, result);
        return result;
    }

    /** 依次探测全部已启用模型。 */
    public List<AiModelHealthStatus> checkAll() {
        configService.listProfiles().stream().filter(AiModelProfileView::enabled).forEach(profile -> check(profile.id()));
        return list();
    }

    /**
     * 按最近健康状态重新排列场景路由：健康和未知模型优先，近期不可用模型放到最后。
     * 未执行探测时保持用户配置的主备顺序。
     */
    public List<AiRuntimeConfig> orderByHealth(List<AiRuntimeConfig> configs) {
        return configs.stream().sorted(Comparator.comparingInt(this::healthRank)).toList();
    }

    private int healthRank(AiRuntimeConfig config) {
        AiModelHealthStatus status = statuses.values().stream().filter(item -> item.model().equals(config.model()))
                .max(Comparator.comparing(AiModelHealthStatus::checkedAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElse(null);
        if (status == null || status.checkedAt() == null || status.checkedAt().isBefore(LocalDateTime.now().minus(HEALTH_TTL))) return 1;
        return "healthy".equals(status.status()) ? 0 : 2;
    }

    private RestClient client(AiRuntimeConfig config) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(8));
        factory.setReadTimeout(Duration.ofSeconds(20));
        return RestClient.builder().baseUrl(config.apiBase().replaceAll("/+$", ""))
                .requestFactory(factory).defaultHeader("Authorization", "Bearer " + config.apiKey()).build();
    }

    private void validate(AiRuntimeConfig config) {
        if (!config.enabled() || config.apiKey() == null || config.apiBase() == null || "mock".equalsIgnoreCase(config.provider())) {
            throw new IllegalStateException("真实模型未启用或缺少连接配置");
        }
    }

    private AiModelHealthStatus status(AiModelProfileView profile, String status, long latency, String message) {
        return new AiModelHealthStatus(profile.id(), profile.name(), profile.model(), status, latency, message, LocalDateTime.now());
    }

    private long elapsed(long startedAt) {
        return Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
    }

    private String shortMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName()
                : message.substring(0, Math.min(message.length(), 240));
    }
}
