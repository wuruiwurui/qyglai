package com.qyglai.automation.controller;

import java.util.List;

import com.qyglai.automation.dto.AutomationModule;
import com.qyglai.automation.service.AutomationModuleService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/modules")
public class ModuleController {

    private final AutomationModuleService moduleService;

    public ModuleController(AutomationModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @GetMapping
    public List<AutomationModule> listModules() {
        return moduleService.listEnabledModules();
    }
}
