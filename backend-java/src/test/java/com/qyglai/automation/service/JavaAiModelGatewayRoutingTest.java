package com.qyglai.automation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qyglai.automation.dto.AiRuntimeConfig;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class JavaAiModelGatewayRoutingTest {

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) server.stop(0);
    }

    @Test
    void shouldSwitchToFallbackModelWhenPrimaryFails() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/chat/completions", exchange -> {
            byte[] body = "{\"choices\":[{\"message\":{\"content\":\"fallback response\"}}]}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        JavaAiModelConfigService configService = mock(JavaAiModelConfigService.class);
        AiRuntimeConfig primary = config("http://127.0.0.1:1", "primary-model");
        AiRuntimeConfig fallback = config("http://127.0.0.1:" + server.getAddress().getPort(), "fallback-model");
        when(configService.resolveConfigs("boss_query")).thenReturn(List.of(primary, fallback));
        when(configService.getConfig()).thenReturn(primary);
        JavaAiModelGateway gateway = new JavaAiModelGateway(configService, new ObjectMapper());

        String result = gateway.generateText("boss_query", "system", "question", "local fallback");

        assertThat(result).isEqualTo("fallback response");
        assertThat(gateway.status().model()).isEqualTo("fallback-model");
        assertThat(gateway.status().lastFallbackReason()).contains("已自动切换备用模型");
    }

    private AiRuntimeConfig config(String baseUrl, String model) {
        return new AiRuntimeConfig("openai", baseUrl, "test-key", model, true, true,
                "rules_first", "rules_first", "test");
    }
}
