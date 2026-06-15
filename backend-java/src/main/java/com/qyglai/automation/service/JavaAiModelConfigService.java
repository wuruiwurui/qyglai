package com.qyglai.automation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qyglai.automation.dto.AiModelProfile;
import com.qyglai.automation.dto.AiModelProfileView;
import com.qyglai.automation.dto.AiModelProfilesStore;
import com.qyglai.automation.dto.AiRuntimeConfig;
import com.qyglai.automation.dto.AiRuntimeConfigView;
import com.qyglai.automation.dto.AiScenarioRoute;
import com.qyglai.automation.dto.AiScenarioRouteView;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Java AI 多模型配置服务，负责模型配置保存、切换和旧配置迁移。
 */
@Service
public class JavaAiModelConfigService {

    private final ObjectMapper objectMapper;
    private final Path legacyConfigPath = Path.of("runtime", "model_config.json").toAbsolutePath().normalize();
    private final Path profilesPath = Path.of("runtime", "model_profiles.json").toAbsolutePath().normalize();

    public JavaAiModelConfigService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 获取当前正在使用的运行配置，模型网关的所有调用均通过此方法读取当前模型。
     */
    public synchronized AiRuntimeConfig getConfig() {
        AiModelProfile profile = currentProfile(readStore());
        if (profile == null) return defaultConfig();
        return toRuntimeConfig(profile);
    }

    /**
     * 按配置ID获取包含真实密钥的运行配置，仅供服务端模型网关与健康检查使用。
     */
    public synchronized AiRuntimeConfig getConfig(String profileId) {
        AiModelProfile profile = readStore().profiles().stream().filter(item -> item.id().equals(profileId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("模型配置不存在"));
        return toRuntimeConfig(profile);
    }

    /**
     * 查询全部模型配置。
     */
    public synchronized List<AiModelProfileView> listProfiles() {
        AiModelProfilesStore store = readStore();
        return store.profiles().stream().map(profile -> view(profile, profile.id().equals(store.currentProfileId()))).toList();
    }

    /**
     * 新增或更新模型配置。API Key留空时保留原密钥。
     */
    public synchronized AiModelProfileView saveProfile(AiModelProfile request) {
        AiModelProfilesStore store = readStore();
        List<AiModelProfile> profiles = new ArrayList<>(store.profiles());
        String id = request.id() == null || request.id().isBlank() ? UUID.randomUUID().toString() : request.id();
        AiModelProfile existing = profiles.stream().filter(item -> item.id().equals(id)).findFirst().orElse(null);
        String apiKey = request.apiKey() == null || request.apiKey().isBlank()
                ? existing == null ? null : existing.apiKey()
                : request.apiKey();
        AiModelProfile saved = new AiModelProfile(id, required(request.name(), "模型配置名称"),
                required(request.provider(), "供应商"), required(request.apiBase(), "API地址"), apiKey,
                required(request.model(), "模型或Endpoint ID"), request.enabled(), request.textGenerationEnabled(),
                defaultValue(request.fileExtractionMode(), "rules_first"),
                defaultValue(request.contractRiskMode(), "rules_first"), request.remark());
        profiles.removeIf(item -> item.id().equals(id));
        profiles.add(saved);
        String currentId = store.currentProfileId() == null ? id : store.currentProfileId();
        writeStore(new AiModelProfilesStore(currentId, profiles, store.scenarioRoutes()));
        return view(saved, id.equals(currentId));
    }

    /**
     * 切换当前使用模型。
     */
    public synchronized AiModelProfileView switchCurrent(String id) {
        AiModelProfilesStore store = readStore();
        AiModelProfile profile = store.profiles().stream().filter(item -> item.id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("模型配置不存在"));
        if (!profile.enabled()) throw new IllegalArgumentException("请先启用该模型配置");
        writeStore(new AiModelProfilesStore(id, store.profiles(), store.scenarioRoutes()));
        return view(profile, true);
    }

    /**
     * 删除非当前模型配置。
     */
    public synchronized void deleteProfile(String id) {
        AiModelProfilesStore store = readStore();
        if (id.equals(store.currentProfileId())) throw new IllegalArgumentException("当前使用模型不能删除，请先切换模型");
        List<AiModelProfile> profiles = new ArrayList<>(store.profiles());
        if (!profiles.removeIf(item -> item.id().equals(id))) throw new IllegalArgumentException("模型配置不存在");
        List<AiScenarioRoute> routes = store.scenarioRoutes().stream()
                .filter(route -> !id.equals(route.primaryProfileId()))
                .map(route -> new AiScenarioRoute(route.scenario(), route.primaryProfileId(),
                        route.fallbackProfileIds().stream().filter(fallbackId -> !id.equals(fallbackId)).toList()))
                .toList();
        writeStore(new AiModelProfilesStore(store.currentProfileId(), profiles, routes));
    }

    /**
     * 兼容旧单模型配置保存接口。
     */
    public synchronized AiRuntimeConfigView save(AiRuntimeConfig request) {
        AiModelProfilesStore store = readStore();
        AiModelProfile current = currentProfile(store);
        AiModelProfile saved = new AiModelProfile(current == null ? null : current.id(),
                current == null ? request.provider() + " / " + request.model() : current.name(),
                request.provider(), request.apiBase(), request.apiKey(), request.model(), request.enabled(),
                request.textGenerationEnabled(), request.fileExtractionMode(), request.contractRiskMode(), request.remark());
        saveProfile(saved);
        return view();
    }

    public synchronized AiRuntimeConfigView view() {
        AiRuntimeConfig config = getConfig();
        return new AiRuntimeConfigView(config.provider(), config.apiBase(), mask(config.apiKey()), config.model(),
                config.enabled(), config.textGenerationEnabled(), config.fileExtractionMode(),
                config.contractRiskMode(), config.remark());
    }

    private AiModelProfilesStore readStore() {
        if (Files.exists(profilesPath)) {
            try {
                AiModelProfilesStore store = objectMapper.readValue(profilesPath.toFile(), AiModelProfilesStore.class);
                return new AiModelProfilesStore(store.currentProfileId(),
                        store.profiles() == null ? List.of() : store.profiles(),
                        store.scenarioRoutes() == null ? List.of() : store.scenarioRoutes());
            } catch (IOException ex) {
                throw new IllegalStateException("读取AI多模型配置失败: " + ex.getMessage(), ex);
            }
        }
        return migrateLegacyConfig();
    }

    private AiModelProfilesStore migrateLegacyConfig() {
        AiRuntimeConfig legacy = readLegacyConfig();
        AiModelProfile profile = new AiModelProfile(UUID.randomUUID().toString(),
                legacy.provider() + " / " + legacy.model(), legacy.provider(), legacy.apiBase(), legacy.apiKey(),
                legacy.model(), legacy.enabled(), legacy.textGenerationEnabled(), legacy.fileExtractionMode(),
                legacy.contractRiskMode(), legacy.remark());
        AiModelProfilesStore store = new AiModelProfilesStore(profile.id(), List.of(profile), List.of());
        writeStore(store);
        return store;
    }

    private AiRuntimeConfig readLegacyConfig() {
        if (Files.exists(legacyConfigPath)) {
            try {
                return objectMapper.readValue(legacyConfigPath.toFile(), AiRuntimeConfig.class);
            } catch (IOException ex) {
                throw new IllegalStateException("读取旧AI模型配置失败: " + ex.getMessage(), ex);
            }
        }
        return defaultConfig();
    }

    private void writeStore(AiModelProfilesStore store) {
        try {
            Files.createDirectories(profilesPath.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(profilesPath.toFile(), store);
        } catch (IOException ex) {
            throw new IllegalStateException("保存AI多模型配置失败: " + ex.getMessage(), ex);
        }
    }

    private AiModelProfile currentProfile(AiModelProfilesStore store) {
        if (store.currentProfileId() == null) return store.profiles().stream().findFirst().orElse(null);
        return store.profiles().stream().filter(item -> item.id().equals(store.currentProfileId())).findFirst().orElse(null);
    }

    /**
     * 查询全部场景模型路由。
     */
    public synchronized List<AiScenarioRouteView> listScenarioRoutes() {
        AiModelProfilesStore store = readStore();
        return store.scenarioRoutes().stream().map(route -> routeView(route, store.profiles())).toList();
    }

    /**
     * 保存场景模型路由。
     */
    public synchronized AiScenarioRouteView saveScenarioRoute(AiScenarioRoute request) {
        AiModelProfilesStore store = readStore();
        String scenario = required(request.scenario(), "场景编码");
        AiModelProfile primary = requireEnabledProfile(store.profiles(), request.primaryProfileId());
        List<String> fallbacks = request.fallbackProfileIds() == null ? List.of()
                : request.fallbackProfileIds().stream().filter(id -> id != null && !id.isBlank())
                .filter(id -> !id.equals(primary.id())).distinct().toList();
        fallbacks.forEach(id -> requireEnabledProfile(store.profiles(), id));
        AiScenarioRoute saved = new AiScenarioRoute(scenario, primary.id(), fallbacks);
        List<AiScenarioRoute> routes = new ArrayList<>(store.scenarioRoutes());
        routes.removeIf(route -> scenario.equals(route.scenario()));
        routes.add(saved);
        writeStore(new AiModelProfilesStore(store.currentProfileId(), store.profiles(), routes));
        return routeView(saved, store.profiles());
    }

    /**
     * 删除场景路由，删除后该场景恢复使用当前模型。
     */
    public synchronized void deleteScenarioRoute(String scenario) {
        AiModelProfilesStore store = readStore();
        List<AiScenarioRoute> routes = new ArrayList<>(store.scenarioRoutes());
        routes.removeIf(route -> scenario.equals(route.scenario()));
        writeStore(new AiModelProfilesStore(store.currentProfileId(), store.profiles(), routes));
    }

    /**
     * 按场景解析可调用模型，主模型失败后按列表顺序尝试备用模型。
     */
    public synchronized List<AiRuntimeConfig> resolveConfigs(String scenario) {
        AiModelProfilesStore store = readStore();
        AiScenarioRoute route = store.scenarioRoutes().stream()
                .filter(item -> item.scenario().equals(scenario)).findFirst().orElse(null);
        List<AiModelProfile> selected = new ArrayList<>();
        if (route != null) {
            addEnabled(selected, store.profiles(), route.primaryProfileId());
            route.fallbackProfileIds().forEach(id -> addEnabled(selected, store.profiles(), id));
        }
        if (selected.isEmpty()) {
            AiModelProfile current = currentProfile(store);
            if (current != null && current.enabled()) selected.add(current);
        }
        return selected.stream().map(this::toRuntimeConfig).toList();
    }

    private void addEnabled(List<AiModelProfile> selected, List<AiModelProfile> profiles, String id) {
        profiles.stream().filter(item -> item.id().equals(id) && item.enabled())
                .filter(item -> selected.stream().noneMatch(existing -> existing.id().equals(item.id())))
                .findFirst().ifPresent(selected::add);
    }

    private AiModelProfile requireEnabledProfile(List<AiModelProfile> profiles, String id) {
        return profiles.stream().filter(item -> item.id().equals(id) && item.enabled()).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("路由模型不存在或未启用: " + id));
    }

    private AiScenarioRouteView routeView(AiScenarioRoute route, List<AiModelProfile> profiles) {
        AiModelProfile primary = profiles.stream().filter(item -> item.id().equals(route.primaryProfileId())).findFirst().orElse(null);
        List<String> fallbackModels = route.fallbackProfileIds().stream()
                .map(id -> profiles.stream().filter(item -> item.id().equals(id)).findFirst()
                        .map(AiModelProfile::name).orElse("已删除模型"))
                .toList();
        return new AiScenarioRouteView(route.scenario(), route.primaryProfileId(),
                primary == null ? "已删除模型" : primary.name(), route.fallbackProfileIds(), fallbackModels);
    }

    private AiRuntimeConfig toRuntimeConfig(AiModelProfile profile) {
        return new AiRuntimeConfig(profile.provider(), profile.apiBase(), profile.apiKey(), profile.model(),
                profile.enabled(), profile.textGenerationEnabled(), profile.fileExtractionMode(),
                profile.contractRiskMode(), profile.remark());
    }

    private AiModelProfileView view(AiModelProfile profile, boolean current) {
        return new AiModelProfileView(profile.id(), profile.name(), profile.provider(), profile.apiBase(),
                mask(profile.apiKey()), profile.model(), profile.enabled(), current, profile.textGenerationEnabled(),
                profile.fileExtractionMode(), profile.contractRiskMode(), profile.remark());
    }

    private AiRuntimeConfig defaultConfig() {
        return new AiRuntimeConfig("mock", null, null, "mock-local-model", false, true,
                "rules_first", "rules_first", "Java默认配置");
    }

    private String required(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + "不能为空");
        return value.trim();
    }

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String mask(String key) {
        if (key == null || key.isBlank()) return null;
        if (key.length() <= 8) return "*".repeat(key.length());
        return key.substring(0, 4) + "*".repeat(key.length() - 8) + key.substring(key.length() - 4);
    }
}
