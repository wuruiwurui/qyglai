package com.qyglai.automation.service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qyglai.automation.entity.AuditLogEntity;
import com.qyglai.automation.mapper.AuditLogMapper;
import org.springframework.stereotype.Service;

/**
 * 审计查询服务。
 */
@Service
public class AuditQueryService {

    private final AuditLogMapper auditLogMapper;

    public AuditQueryService(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    /**
     * 查询最新审计日志。
     *
     * @return 审计日志列表
     */
    public List<AuditLogEntity> listLogs() {
        return auditLogMapper.selectList(new LambdaQueryWrapper<AuditLogEntity>().orderByDesc(AuditLogEntity::getCreatedAt).last("LIMIT 100"));
    }
}
