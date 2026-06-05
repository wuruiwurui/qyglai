package com.qyglai.automation.controller;

import java.util.List;

import com.qyglai.automation.common.ApiResponse;
import com.qyglai.automation.dto.KnowledgeAnswer;
import com.qyglai.automation.dto.KnowledgeQueryRequest;
import com.qyglai.automation.entity.KbSpaceEntity;
import com.qyglai.automation.service.AutomationWorkspaceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/kb")
public class KnowledgeController {

    private final AutomationWorkspaceService service;

    public KnowledgeController(AutomationWorkspaceService service) {
        this.service = service;
    }

    @PostMapping("/query")
    public ApiResponse<KnowledgeAnswer> query(@Valid @RequestBody KnowledgeQueryRequest request) {
        return ApiResponse.ok(service.queryKnowledge(request));
    }

    /**
     * 查询知识库空间。
     *
     * @return 知识库空间列表
     */
    @GetMapping("/spaces")
    public ApiResponse<List<KbSpaceEntity>> spaces() {
        return ApiResponse.ok(service.listKnowledgeSpaces());
    }
}
