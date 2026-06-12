package com.qyglai.automation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qyglai.automation.dto.AiRuntimeStatus;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AiBusinessApplicationServiceTest {

    private JavaAiModelGateway modelGateway;
    private AiBusinessApplicationService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        modelGateway = mock(JavaAiModelGateway.class);
        service = new AiBusinessApplicationService(modelGateway, mock(AiCallLogService.class),
                mock(BossBusinessContextService.class), objectMapper);
    }

    @Test
    void shouldUseStructuredModelTicketDecision() throws Exception {
        when(modelGateway.generateJson(anyString(), anyString(), anyString())).thenReturn(objectMapper.readTree("""
                {"category":"交付投诉","priority":"P1","sentiment":"negative","suggestedOwner":"客服主管",
                 "confidence":0.91,"replySuggestion":"立即核实交付节点并反馈。"}
                """));
        when(modelGateway.status()).thenReturn(status("success", null));

        AiBusinessApplicationService.TicketDecision result = service.classifyTicket("交付严重延期");

        assertThat(result.modelSuccess()).isTrue();
        assertThat(result.category()).isEqualTo("交付投诉");
        assertThat(result.priority()).isEqualTo("P1");
    }

    @Test
    void shouldKeepJavaDifferenceAndFallbackWhenModelFails() {
        when(modelGateway.generateJson(anyString(), anyString(), anyString())).thenReturn(null);
        when(modelGateway.status()).thenReturn(status("fallback", "timeout"));

        AiBusinessApplicationService.ReconciliationDecision result = service.explainReconciliation(
                "供应商", new BigDecimal("100"), new BigDecimal("80"), new BigDecimal("20"));

        assertThat(result.modelSuccess()).isFalse();
        assertThat(result.status()).isEqualTo("difference");
        assertThat(result.risks()).isNotEmpty();
    }

    private AiRuntimeStatus status(String callStatus, String reason) {
        return new AiRuntimeStatus("doubao", "endpoint-id", true, "https://example.com", true,
                "hybrid", "hybrid", callStatus, reason);
    }
}
