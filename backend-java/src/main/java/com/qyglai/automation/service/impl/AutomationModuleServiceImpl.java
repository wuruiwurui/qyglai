package com.qyglai.automation.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qyglai.automation.dto.AutomationModule;
import com.qyglai.automation.entity.AutomationModuleEntity;
import com.qyglai.automation.mapper.AutomationModuleMapper;
import com.qyglai.automation.service.AutomationModuleService;
import org.springframework.stereotype.Service;

@Service
public class AutomationModuleServiceImpl implements AutomationModuleService {

    private final AutomationModuleMapper moduleMapper;

    public AutomationModuleServiceImpl(AutomationModuleMapper moduleMapper) {
        this.moduleMapper = moduleMapper;
    }

    @Override
    public List<AutomationModule> listEnabledModules() {
        return moduleMapper.selectList(new LambdaQueryWrapper<AutomationModuleEntity>()
                        .eq(AutomationModuleEntity::getStatus, "enabled")
                        .orderByAsc(AutomationModuleEntity::getSortOrder))
                .stream()
                .map(this::toDto)
                .toList();
    }

    private AutomationModule toDto(AutomationModuleEntity entity) {
        return new AutomationModule(
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getOwner(),
                entity.getStatus()
        );
    }
}

