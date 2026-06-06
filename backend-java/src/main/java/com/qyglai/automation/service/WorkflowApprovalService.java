package com.qyglai.automation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qyglai.automation.dto.WorkflowActionRequest;
import com.qyglai.automation.dto.WorkflowDefinitionSaveRequest;
import com.qyglai.automation.dto.WorkflowInstanceDetail;
import com.qyglai.automation.dto.WorkflowInstanceSummary;
import com.qyglai.automation.dto.WorkflowStartRequest;
import com.qyglai.automation.entity.WorkflowActionLogEntity;
import com.qyglai.automation.entity.WorkflowDefinitionEntity;
import com.qyglai.automation.entity.WorkflowInstanceEntity;
import com.qyglai.automation.entity.WorkflowTaskEntity;
import com.qyglai.automation.mapper.WorkflowActionLogMapper;
import com.qyglai.automation.mapper.WorkflowDefinitionMapper;
import com.qyglai.automation.mapper.WorkflowInstanceMapper;
import com.qyglai.automation.mapper.WorkflowTaskMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 企业审批工作流服务。
 *
 * <p>流程定义采用顺序节点模型，负责发起、多级审批、驳回、转交及完整历史追踪。</p>
 */
@Service
public class WorkflowApprovalService {

    private final WorkflowDefinitionMapper definitionMapper;
    private final WorkflowInstanceMapper instanceMapper;
    private final WorkflowTaskMapper taskMapper;
    private final WorkflowActionLogMapper actionLogMapper;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public WorkflowApprovalService(WorkflowDefinitionMapper definitionMapper,
                                   WorkflowInstanceMapper instanceMapper,
                                   WorkflowTaskMapper taskMapper,
                                   WorkflowActionLogMapper actionLogMapper,
                                   AuditService auditService,
                                   ObjectMapper objectMapper) {
        this.definitionMapper = definitionMapper;
        this.instanceMapper = instanceMapper;
        this.taskMapper = taskMapper;
        this.actionLogMapper = actionLogMapper;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    /** 查询启用和停用的全部流程定义。 */
    public List<WorkflowDefinitionEntity> listDefinitions() {
        return definitionMapper.selectList(new LambdaQueryWrapper<WorkflowDefinitionEntity>()
                .orderByAsc(WorkflowDefinitionEntity::getWorkflowCode)
                .orderByDesc(WorkflowDefinitionEntity::getVersionNo));
    }

    /** 保存新版本流程定义，旧版本实例不受影响。 */
    @Transactional(rollbackFor = Exception.class)
    public WorkflowDefinitionEntity saveDefinition(WorkflowDefinitionSaveRequest request) {
        parseNodes(request.definitionJson());
        WorkflowDefinitionEntity latest = definitionMapper.selectOne(new LambdaQueryWrapper<WorkflowDefinitionEntity>()
                .eq(WorkflowDefinitionEntity::getWorkflowCode, request.workflowCode())
                .orderByDesc(WorkflowDefinitionEntity::getVersionNo)
                .last("LIMIT 1"));
        WorkflowDefinitionEntity entity = new WorkflowDefinitionEntity();
        entity.setWorkflowCode(request.workflowCode());
        entity.setWorkflowName(request.workflowName());
        entity.setScenario(request.scenario());
        entity.setVersionNo(latest == null ? 1 : latest.getVersionNo() + 1);
        entity.setDefinitionJson(request.definitionJson());
        entity.setStatus(request.status() == null || request.status().isBlank() ? "enabled" : request.status());
        definitionMapper.insert(entity);
        auditService.record("WORKFLOW_DEFINITION_SAVE", "保存流程定义", "workflow_definition", entity.getId());
        return entity;
    }

    /** 发起审批并创建第一个待办任务。 */
    @Transactional(rollbackFor = Exception.class)
    public WorkflowInstanceSummary start(WorkflowStartRequest request, Long initiatorUserId) {
        WorkflowDefinitionEntity definition = findOrCreateDefinition(request.workflowCode());
        List<NodeDefinition> nodes = parseNodes(definition.getDefinitionJson());
        NodeDefinition first = nodes.getFirst();

        WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
        instance.setId(IdWorker.getId());
        instance.setDefinitionId(definition.getId());
        instance.setBusinessType(stringVariable(request.variables(), "businessType", definition.getScenario()));
        instance.setBusinessId(longVariable(request.variables(), "businessId"));
        instance.setInitiatorUserId(initiatorUserId);
        instance.setCurrentNode(first.code());
        instance.setVariablesJson(toJson(request.variables()));
        instance.setStatus("running");
        instanceMapper.insert(instance);

        WorkflowTaskEntity task = createTask(instance.getId(), first, initiatorUserId);
        recordAction(instance.getId(), task.getId(), first.code(), "start", initiatorUserId, null,
                stringVariable(request.variables(), "title", "发起审批"));
        auditService.record("WORKFLOW_START", "发起审批流程", "workflow_instance", instance.getId());
        return summary(instance, definition.getWorkflowCode());
    }

    /** 查询全部流程实例。 */
    public List<WorkflowInstanceEntity> listInstances() {
        return instanceMapper.selectList(new LambdaQueryWrapper<WorkflowInstanceEntity>()
                .orderByDesc(WorkflowInstanceEntity::getStartedAt));
    }

    /** 查询当前用户待办，管理员可通过传入空用户查看全部。 */
    public List<WorkflowTaskEntity> listPendingTasks(Long userId, boolean viewAll) {
        LambdaQueryWrapper<WorkflowTaskEntity> query = new LambdaQueryWrapper<WorkflowTaskEntity>()
                .eq(WorkflowTaskEntity::getStatus, "pending")
                .orderByAsc(WorkflowTaskEntity::getDueTime)
                .orderByDesc(WorkflowTaskEntity::getCreatedAt);
        if (!viewAll) {
            query.eq(WorkflowTaskEntity::getAssigneeUserId, userId);
        }
        return taskMapper.selectList(query);
    }

    /** 查询流程实例、任务节点和审批历史。 */
    public WorkflowInstanceDetail detail(Long instanceId) {
        WorkflowInstanceEntity instance = requireInstance(instanceId);
        WorkflowDefinitionEntity definition = definitionMapper.selectById(instance.getDefinitionId());
        List<WorkflowTaskEntity> tasks = taskMapper.selectList(new LambdaQueryWrapper<WorkflowTaskEntity>()
                .eq(WorkflowTaskEntity::getInstanceId, instanceId)
                .orderByAsc(WorkflowTaskEntity::getCreatedAt));
        List<WorkflowActionLogEntity> history = actionLogMapper.selectList(new LambdaQueryWrapper<WorkflowActionLogEntity>()
                .eq(WorkflowActionLogEntity::getInstanceId, instanceId)
                .orderByAsc(WorkflowActionLogEntity::getCreatedAt));
        return new WorkflowInstanceDetail(instance, definition, tasks, history);
    }

    /** 处理审批任务。 */
    @Transactional(rollbackFor = Exception.class)
    public WorkflowInstanceDetail act(Long taskId, WorkflowActionRequest request, Long operatorUserId, boolean administrator) {
        WorkflowTaskEntity task = taskMapper.selectById(taskId);
        if (task == null || !"pending".equals(task.getStatus())) {
            throw new IllegalArgumentException("审批任务不存在或已处理");
        }
        if (!administrator && !operatorUserId.equals(task.getAssigneeUserId())) {
            throw new IllegalArgumentException("当前用户不是该任务处理人");
        }
        return switch (request.action().toLowerCase()) {
            case "approve" -> approve(task, request.comment(), operatorUserId);
            case "reject" -> reject(task, request.comment(), operatorUserId);
            case "transfer" -> transfer(task, request.comment(), request.targetUserId(), operatorUserId);
            default -> throw new IllegalArgumentException("不支持的审批动作");
        };
    }

    private WorkflowInstanceDetail approve(WorkflowTaskEntity task, String comment, Long operatorUserId) {
        completeTask(task, "approved", comment, operatorUserId);
        recordAction(task.getInstanceId(), task.getId(), task.getNodeCode(), "approved", operatorUserId, null, comment);
        WorkflowInstanceEntity instance = requireInstance(task.getInstanceId());
        WorkflowDefinitionEntity definition = definitionMapper.selectById(instance.getDefinitionId());
        List<NodeDefinition> nodes = parseNodes(definition.getDefinitionJson());
        int currentIndex = indexOf(nodes, task.getNodeCode());
        if (currentIndex + 1 >= nodes.size()) {
            instance.setStatus("approved");
            instance.setCurrentNode("DONE");
            instance.setEndedAt(LocalDateTime.now());
            instanceMapper.updateById(instance);
            recordAction(instance.getId(), task.getId(), task.getNodeCode(), "complete", operatorUserId, null, "流程审批完成");
        } else {
            NodeDefinition next = nodes.get(currentIndex + 1);
            WorkflowTaskEntity nextTask = createTask(instance.getId(), next, instance.getInitiatorUserId());
            instance.setCurrentNode(next.code());
            instanceMapper.updateById(instance);
            recordAction(instance.getId(), nextTask.getId(), next.code(), "arrive", operatorUserId, nextTask.getAssigneeUserId(), "进入下一审批节点");
        }
        auditService.record("WORKFLOW_APPROVE", "审批通过", "workflow_task", task.getId());
        return detail(instance.getId());
    }

    private WorkflowInstanceDetail reject(WorkflowTaskEntity task, String comment, Long operatorUserId) {
        completeTask(task, "rejected", comment, operatorUserId);
        recordAction(task.getInstanceId(), task.getId(), task.getNodeCode(), "rejected", operatorUserId, null, comment);
        WorkflowInstanceEntity instance = requireInstance(task.getInstanceId());
        instance.setStatus("rejected");
        instance.setCurrentNode("REJECTED");
        instance.setEndedAt(LocalDateTime.now());
        instanceMapper.updateById(instance);
        auditService.record("WORKFLOW_REJECT", "审批驳回", "workflow_task", task.getId());
        return detail(instance.getId());
    }

    private WorkflowInstanceDetail transfer(WorkflowTaskEntity task, String comment, Long targetUserId, Long operatorUserId) {
        if (targetUserId == null) {
            throw new IllegalArgumentException("转交时必须指定目标用户");
        }
        completeTask(task, "transferred", comment, operatorUserId);
        WorkflowTaskEntity replacement = new WorkflowTaskEntity();
        replacement.setInstanceId(task.getInstanceId());
        replacement.setNodeCode(task.getNodeCode());
        replacement.setNodeName(task.getNodeName());
        replacement.setAssigneeUserId(targetUserId);
        replacement.setTaskType("manual");
        replacement.setStatus("pending");
        replacement.setDueTime(task.getDueTime());
        taskMapper.insert(replacement);
        recordAction(task.getInstanceId(), replacement.getId(), task.getNodeCode(), "transfer", operatorUserId, targetUserId, comment);
        auditService.record("WORKFLOW_TRANSFER", "转交审批任务", "workflow_task", task.getId());
        return detail(task.getInstanceId());
    }

    private void completeTask(WorkflowTaskEntity task, String status, String comment, Long operatorUserId) {
        task.setStatus(status);
        task.setCompletedAt(LocalDateTime.now());
        task.setResultJson(toJson(Map.of("action", status, "comment", comment == null ? "" : comment, "operatorUserId", operatorUserId)));
        taskMapper.updateById(task);
    }

    private WorkflowTaskEntity createTask(Long instanceId, NodeDefinition node, Long fallbackUserId) {
        WorkflowTaskEntity task = new WorkflowTaskEntity();
        task.setInstanceId(instanceId);
        task.setNodeCode(node.code());
        task.setNodeName(node.name());
        task.setAssigneeUserId(node.assigneeUserId() == null ? fallbackUserId : node.assigneeUserId());
        task.setTaskType("manual");
        task.setStatus("pending");
        task.setDueTime(LocalDateTime.now().plusHours(node.dueHours()));
        taskMapper.insert(task);
        return task;
    }

    private void recordAction(Long instanceId, Long taskId, String nodeCode, String action, Long operatorUserId,
                              Long targetUserId, String comment) {
        WorkflowActionLogEntity log = new WorkflowActionLogEntity();
        log.setInstanceId(instanceId);
        log.setTaskId(taskId);
        log.setNodeCode(nodeCode);
        log.setAction(action);
        log.setOperatorUserId(operatorUserId);
        log.setTargetUserId(targetUserId);
        log.setComment(comment);
        actionLogMapper.insert(log);
    }

    private WorkflowDefinitionEntity findOrCreateDefinition(String workflowCode) {
        WorkflowDefinitionEntity existing = definitionMapper.selectOne(new LambdaQueryWrapper<WorkflowDefinitionEntity>()
                .eq(WorkflowDefinitionEntity::getWorkflowCode, workflowCode)
                .eq(WorkflowDefinitionEntity::getStatus, "enabled")
                .orderByDesc(WorkflowDefinitionEntity::getVersionNo)
                .last("LIMIT 1"));
        if (existing != null) {
            return existing;
        }
        WorkflowDefinitionEntity definition = new WorkflowDefinitionEntity();
        definition.setWorkflowCode(workflowCode);
        definition.setWorkflowName(workflowCode + "审批流程");
        definition.setScenario(workflowCode);
        definition.setVersionNo(1);
        definition.setDefinitionJson("""
                {"nodes":[
                  {"code":"department_review","name":"部门负责人审批","dueHours":24},
                  {"code":"finance_review","name":"财务审批","dueHours":24},
                  {"code":"final_review","name":"最终审批","dueHours":48}
                ]}""");
        definition.setStatus("enabled");
        definitionMapper.insert(definition);
        return definition;
    }

    private List<NodeDefinition> parseNodes(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode nodeArray = root.path("nodes");
            if (!nodeArray.isArray() || nodeArray.isEmpty()) {
                throw new IllegalArgumentException("流程定义至少需要一个审批节点");
            }
            List<NodeDefinition> result = new ArrayList<>();
            for (JsonNode node : nodeArray) {
                if (node.isTextual()) {
                    result.add(new NodeDefinition(node.asText(), node.asText(), null, 24));
                } else {
                    String code = node.path("code").asText();
                    if (code.isBlank()) {
                        throw new IllegalArgumentException("审批节点编码不能为空");
                    }
                    result.add(new NodeDefinition(code, node.path("name").asText(code),
                            node.hasNonNull("assigneeUserId") ? node.path("assigneeUserId").asLong() : null,
                            Math.max(1, node.path("dueHours").asInt(24))));
                }
            }
            return result;
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("流程定义JSON格式错误", ex);
        }
    }

    private int indexOf(List<NodeDefinition> nodes, String code) {
        for (int index = 0; index < nodes.size(); index++) {
            if (nodes.get(index).code().equals(code)) {
                return index;
            }
        }
        throw new IllegalArgumentException("当前审批节点不在流程定义中");
    }

    private WorkflowInstanceEntity requireInstance(Long instanceId) {
        WorkflowInstanceEntity instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new IllegalArgumentException("流程实例不存在");
        }
        return instance;
    }

    private WorkflowInstanceSummary summary(WorkflowInstanceEntity instance, String workflowCode) {
        return new WorkflowInstanceSummary(String.valueOf(instance.getId()), workflowCode, instance.getCurrentNode(),
                instance.getStatus(), java.time.Instant.now());
    }

    private Long longVariable(Map<String, Object> variables, String key) {
        if (variables == null || variables.get(key) == null || String.valueOf(variables.get(key)).isBlank()) {
            return null;
        }
        return Long.valueOf(String.valueOf(variables.get(key)));
    }

    private String stringVariable(Map<String, Object> variables, String key, String fallback) {
        if (variables == null || variables.get(key) == null || String.valueOf(variables.get(key)).isBlank()) {
            return fallback;
        }
        return String.valueOf(variables.get(key));
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("JSON序列化失败", ex);
        }
    }

    /** 流程定义中的顺序审批节点。 */
    private record NodeDefinition(String code, String name, Long assigneeUserId, int dueHours) {
    }
}
