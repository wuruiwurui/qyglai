package com.qyglai.automation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qyglai.automation.dto.WorkflowDefinitionSaveRequest;
import com.qyglai.automation.mapper.WorkflowActionLogMapper;
import com.qyglai.automation.mapper.WorkflowDefinitionMapper;
import com.qyglai.automation.mapper.WorkflowInstanceMapper;
import com.qyglai.automation.mapper.WorkflowTaskMapper;
import org.junit.jupiter.api.Test;

class WorkflowApprovalServiceDefinitionTest {

    @Test
    void shouldValidateConditionalAndCountersignDefinition() {
        WorkflowApprovalService service = service();
        WorkflowDefinitionSaveRequest request = new WorkflowDefinitionSaveRequest(
                "contract_enhanced", "合同增强审批", "contract", """
                {"nodes":[
                  {"type":"approval","code":"legal_countersign","name":"法务会签",
                   "assigneeUserIds":[1,2],"approvalMode":"all","dueHours":24},
                  {"type":"approval","code":"large_amount_review","name":"大额审批",
                   "conditionVariable":"amount","conditionOperator":"gte","conditionValue":"100000","dueHours":24},
                  {"type":"cc","code":"cc_finance","name":"抄送财务","assigneeUserIds":[1]}
                ]}
                """, "draft");

        assertThat(service.validateDefinition(request)).isEmpty();
    }

    @Test
    void shouldRejectDuplicateCodesAndInvalidCountersign() {
        WorkflowApprovalService service = service();
        WorkflowDefinitionSaveRequest request = new WorkflowDefinitionSaveRequest(
                "bad_flow", "错误流程", "test", """
                {"nodes":[
                  {"code":"same","name":"会签","assigneeUserIds":[1],"approvalMode":"all"},
                  {"code":"same","name":"重复节点"}
                ]}
                """, "draft");

        assertThat(service.validateDefinition(request))
                .anyMatch(issue -> issue.contains("节点编码重复"));
    }

    private WorkflowApprovalService service() {
        return new WorkflowApprovalService(
                mock(WorkflowDefinitionMapper.class), mock(WorkflowInstanceMapper.class),
                mock(WorkflowTaskMapper.class), mock(WorkflowActionLogMapper.class),
                mock(AuditService.class), mock(BusinessApprovalStatusService.class),
                mock(WorkflowAiNodeService.class), new ObjectMapper());
    }
}
