package com.qyglai.automation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qyglai.automation.dto.AiRuntimeStatus;
import com.qyglai.automation.entity.WorkflowInstanceEntity;
import com.qyglai.automation.mapper.ContractRecordMapper;
import com.qyglai.automation.mapper.InvoiceRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorkflowAiNodeServiceTest {

    private JavaAiModelGateway modelGateway;
    private AiCallLogService callLogService;
    private WorkflowAiNodeService service;

    @BeforeEach
    void setUp() {
        modelGateway = mock(JavaAiModelGateway.class);
        callLogService = mock(AiCallLogService.class);
        service = new WorkflowAiNodeService(modelGateway, callLogService,
                mock(ContractRecordMapper.class), mock(InvoiceRecordMapper.class), new ObjectMapper());
    }

    @Test
    void shouldAutoApproveOnlyForHighConfidenceRiskFreeModelResult() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        when(modelGateway.generateJson(anyString(), anyString(), anyString())).thenReturn(mapper.readTree("""
                {"decision":"auto_pass","confidence":0.93,"summary":"业务数据完整","risks":[]}
                """));
        when(modelGateway.status()).thenReturn(status("success", null));

        WorkflowAiNodeService.AiNodeResult result = service.review(instance(), "ai_review", "AI复核");

        assertThat(result.autoApproved()).isTrue();
        assertThat(result.decision()).isEqualTo("auto_pass");
        assertThat(result.confidence()).isEqualTo(0.93);
        verify(callLogService).record(anyString(), anyString(), anyLong(), anyString(), anyString(),
                anyString(), anyString(), anyLong(), any(Boolean.class), isNull());
    }

    @Test
    void shouldFallbackToManualReviewWhenModelFails() {
        when(modelGateway.generateJson(anyString(), anyString(), anyString())).thenReturn(null);
        when(modelGateway.status()).thenReturn(status("fallback", "连接超时"));

        WorkflowAiNodeService.AiNodeResult result = service.review(instance(), "ai_review", "AI复核");

        assertThat(result.autoApproved()).isFalse();
        assertThat(result.decision()).isEqualTo("manual_review");
        assertThat(result.fallbackReason()).isEqualTo("连接超时");
    }

    private WorkflowInstanceEntity instance() {
        WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
        instance.setId(1L);
        instance.setBusinessType("general");
        instance.setBusinessId(2L);
        instance.setVariablesJson("{}");
        return instance;
    }

    private AiRuntimeStatus status(String callStatus, String reason) {
        return new AiRuntimeStatus("doubao", "endpoint-id", true, "https://example.com", true,
                "hybrid", "hybrid", callStatus, reason);
    }
}
