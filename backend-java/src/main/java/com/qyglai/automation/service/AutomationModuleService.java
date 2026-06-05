package com.qyglai.automation.service;

import java.util.List;

import com.qyglai.automation.dto.AutomationModule;

public interface AutomationModuleService {

    List<AutomationModule> listEnabledModules();
}

