package com.qyglai.automation.service;

import com.qyglai.automation.entity.AuditLogEntity;
import com.qyglai.automation.mapper.AuditLogMapper;
import org.springframework.stereotype.Service;

/**
 * 审计日志服务，记录关键业务动作。
 */
@Service
public class AuditService {

    private final AuditLogMapper auditLogMapper;

    public AuditService(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    /**
     * 记录审计日志。
     *
     * @param actionCode 操作编码
     * @param actionName 操作名称
     * @param targetType 目标类型
     * @param targetId 目标ID
     */
    public void record(String actionCode, String actionName, String targetType, Long targetId) {
        recordDetailed(null, "system", actionCode, actionName, targetType, targetId, null, null);
    }

    /**
     * 记录包含操作人和变更快照的详细审计日志。
     *
     * @param operatorUserId 操作人用户ID
     * @param operatorName 操作人名称
     * @param actionCode 操作编码
     * @param actionName 操作名称
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param beforeJson 变更前JSON
     * @param afterJson 变更后JSON
     */
    public void recordDetailed(Long operatorUserId, String operatorName, String actionCode, String actionName,
                               String targetType, Long targetId, String beforeJson, String afterJson) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setOperatorUserId(operatorUserId);
        entity.setOperatorName(operatorName);
        entity.setActionCode(actionCode);
        entity.setActionName(actionName);
        entity.setTargetType(targetType);
        entity.setTargetId(targetId);
        entity.setBeforeJson(beforeJson);
        entity.setAfterJson(afterJson);
        auditLogMapper.insert(entity);
    }
}
