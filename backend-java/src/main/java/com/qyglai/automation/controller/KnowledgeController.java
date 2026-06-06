package com.qyglai.automation.controller;

import java.util.List;

import com.qyglai.automation.common.ApiResponse;
import com.qyglai.automation.dto.KnowledgeAnswer;
import com.qyglai.automation.dto.KnowledgeDocumentIndexRequest;
import com.qyglai.automation.dto.KnowledgeIndexResult;
import com.qyglai.automation.dto.KnowledgeQueryRequest;
import com.qyglai.automation.dto.KnowledgeSearchHit;
import com.qyglai.automation.dto.KnowledgeSpaceCreateRequest;
import com.qyglai.automation.entity.FileAssetEntity;
import com.qyglai.automation.entity.KbDocumentEntity;
import com.qyglai.automation.entity.KbSpaceEntity;
import com.qyglai.automation.service.AutomationWorkspaceService;
import com.qyglai.automation.service.JavaFileParserService;
import com.qyglai.automation.service.KnowledgeRagService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 企业知识库 RAG 接口。
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/kb")
public class KnowledgeController {

    private final KnowledgeRagService ragService;
    private final AutomationWorkspaceService workspaceService;
    private final JavaFileParserService fileParserService;

    public KnowledgeController(KnowledgeRagService ragService, AutomationWorkspaceService workspaceService,
                               JavaFileParserService fileParserService) {
        this.ragService = ragService;
        this.workspaceService = workspaceService;
        this.fileParserService = fileParserService;
    }

    /**
     * 基于知识库检索结果回答问题。
     */
    @PostMapping("/query")
    public ApiResponse<KnowledgeAnswer> query(@Valid @RequestBody KnowledgeQueryRequest request) {
        return ApiResponse.ok(ragService.query(request));
    }

    /**
     * 执行向量检索并返回原始命中切片。
     */
    @GetMapping("/search")
    public ApiResponse<List<KnowledgeSearchHit>> search(@RequestParam String question,
                                                        @RequestParam(required = false) String scope,
                                                        @RequestParam(defaultValue = "5") int topK) {
        return ApiResponse.ok(ragService.search(question, scope, topK));
    }

    /**
     * 创建知识库空间。
     */
    @PostMapping("/spaces")
    public ApiResponse<KbSpaceEntity> createSpace(@Valid @RequestBody KnowledgeSpaceCreateRequest request) {
        return ApiResponse.ok(ragService.createSpace(request));
    }

    /**
     * 查询知识库空间。
     */
    @GetMapping("/spaces")
    public ApiResponse<List<KbSpaceEntity>> spaces() {
        return ApiResponse.ok(ragService.listSpaces());
    }

    /**
     * 将纯文本内容建立知识索引。
     */
    @PostMapping("/documents/text")
    public ApiResponse<KnowledgeIndexResult> indexText(@Valid @RequestBody KnowledgeDocumentIndexRequest request) {
        return ApiResponse.ok(ragService.indexText(request));
    }

    /**
     * 上传文件、解析文本并建立知识索引。
     */
    @PostMapping("/documents/file")
    public ApiResponse<KnowledgeIndexResult> indexFile(@RequestPart("file") MultipartFile file,
                                                       @RequestParam Long spaceId,
                                                       @RequestParam(required = false) String title) {
        FileAssetEntity asset = workspaceService.uploadFile(file, "kb");
        String rawText = fileParserService.parse(file).rawText();
        String documentTitle = title == null || title.isBlank() ? asset.getOriginalName() : title;
        return ApiResponse.ok(ragService.indexFile(spaceId, asset.getId(), documentTitle, rawText));
    }

    /**
     * 查询已建立索引的知识库文档。
     */
    @GetMapping("/documents")
    public ApiResponse<List<KbDocumentEntity>> documents(@RequestParam(required = false) String scope) {
        return ApiResponse.ok(ragService.listDocuments(scope));
    }
}
