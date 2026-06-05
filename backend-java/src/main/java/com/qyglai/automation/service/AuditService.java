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
        AuditLogEntity entity = new AuditLogEntity();
        entity.setOperatorName("system");
        entity.setActionCode(actionCode);
        entity.setActionName(actionName);
        entity.setTargetType(targetType);
        entity.setTargetId(targetId);
        auditLogMapper.insert(entity);
    }
}
