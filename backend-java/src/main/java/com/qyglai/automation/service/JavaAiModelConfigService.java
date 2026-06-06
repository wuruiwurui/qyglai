package com.qyglai.automation.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qyglai.automation.dto.AiRuntimeConfig;
import com.qyglai.automation.dto.AiRuntimeConfigView;
import org.springframework.stereotype.Service;

/**
 * Java AI 模型配置服务。
 */
@Service
public class JavaAiModelConfigService {

    private final ObjectMapper objectMapper;
    private final Path javaConfigPath = Path.of("runtime", "model_config.json").toAbsolutePath().normalize();

    public JavaAiModelConfigService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 读取 Java AI 模型配置。
     */
    public AiRuntimeConfig getConfig() {
        if (Files.exists(javaConfigPath)) {
            try {
                return objectMapper.readValue(javaConfigPath.toFile(), AiRuntimeConfig.class);
            } catch (IOException ex) {
                throw new IllegalStateException("读取AI模型配置失败: " + ex.getMessage(), ex);
            }
        }
        return new AiRuntimeConfig("mock", null, null, "mock-local-model", false, true,
                "rules_first", "rules_first", "Java默认配置");
    }

    /**
     * 保存配置；页面留空 API Key 时保留原密钥。
     */
    public AiRuntimeConfigView save(AiRuntimeConfig request) {
        AiRuntimeConfig current = getConfig();
        String apiKey = request.apiKey() == null || request.apiKey().isBlank() ? current.apiKey() : request.apiKey();
        AiRuntimeConfig saved = new AiRuntimeConfig(request.provider(), request.apiBase(), apiKey, request.model(),
                request.enabled(), request.textGenerationEnabled(), request.fileExtractionMode(),
                request.contractRiskMode(), request.remark());
        try {
            Files.createDirectories(javaConfigPath.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(javaConfigPath.toFile(), saved);
            return view(saved);
        } catch (IOException ex) {
            throw new IllegalStateException("保存AI模型配置失败: " + ex.getMessage(), ex);
        }
    }

    public AiRuntimeConfigView view() {
        return view(getConfig());
    }

    private AiRuntimeConfigView view(AiRuntimeConfig config) {
        return new AiRuntimeConfigView(config.provider(), config.apiBase(), mask(config.apiKey()), config.model(),
                config.enabled(), config.textGenerationEnabled(), config.fileExtractionMode(),
                config.contractRiskMode(), config.remark());
    }

    private String mask(String key) {
        if (key == null || key.isBlank()) return null;
        if (key.length() <= 8) return "*".repeat(key.length());
        return key.substring(0, 4) + "*".repeat(key.length() - 8) + key.substring(key.length() - 4);
    }
}
