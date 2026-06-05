package com.qyglai.automation.controller;

import java.util.List;

import com.qyglai.automation.common.ApiResponse;
import com.qyglai.automation.dto.DocParseSaveRequest;
import com.qyglai.automation.dto.FileAssetDetail;
import com.qyglai.automation.dto.FileFieldConfirmRequest;
import com.qyglai.automation.dto.FileAiProcessResult;
import com.qyglai.automation.entity.DocParseResultEntity;
import com.qyglai.automation.entity.FileAssetEntity;
import com.qyglai.automation.service.AutomationWorkspaceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件资产与文档解析接口。
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/files")
public class FileAssetController {

    private final AutomationWorkspaceService service;

    public FileAssetController(AutomationWorkspaceService service) {
        this.service = service;
    }

    /**
     * 查询文件资产列表。
     *
     * @return 文件资产列表
     */
    @GetMapping
    public ApiResponse<List<FileAssetEntity>> list() {
        return ApiResponse.ok(service.listFiles());
    }

    /**
     * 查询文件解析和业务入库详情。
     *
     * @param id 文件ID
     * @return 文件详情
     */
    @GetMapping("/{id}/detail")
    public ApiResponse<FileAssetDetail> detail(@PathVariable Long id) {
        return ApiResponse.ok(service.getFileDetail(id));
    }

    /**
     * 上传文件并登记文件资产。
     *
     * @param file 上传文件
     * @param businessType 业务类型
     * @return 文件资产实体
     */
    @PostMapping("/upload")
    public ApiResponse<FileAssetEntity> upload(@RequestPart("file") MultipartFile file,
                                               @RequestParam(defaultValue = "general") String businessType) {
        return ApiResponse.ok(service.uploadFile(file, businessType));
    }

    /**
     * 保存文档解析结果。
     *
     * @param request 文档解析结果保存请求
     * @return 文档解析结果实体
     */
    @PostMapping("/parse-results")
    public ApiResponse<DocParseResultEntity> saveParseResult(@Valid @RequestBody DocParseSaveRequest request) {
        return ApiResponse.ok(service.saveParseResult(request.fileId(), request.rawText()));
    }

    /**
     * 上传文件并自动完成AI解析、字段抽取、业务入库和复核任务生成。
     *
     * @param file 上传文件
     * @param businessType 业务类型
     * @return 文件AI处理闭环结果
     */
    @PostMapping("/ai-process")
    public ApiResponse<FileAiProcessResult> aiProcess(@RequestPart("file") MultipartFile file,
                                                       @RequestParam(defaultValue = "general") String businessType) {
        return ApiResponse.ok(service.processFileWithAi(file, businessType));
    }

    /**
     * 人工确认并保存文件抽取字段。
     *
     * @param id 文件ID
     * @param request 字段确认请求
     * @return 文件详情
     */
    @PostMapping("/{id}/confirm-fields")
    public ApiResponse<FileAssetDetail> confirmFields(@PathVariable Long id,
                                                       @Valid @RequestBody FileFieldConfirmRequest request) {
        return ApiResponse.ok(service.confirmFileFields(id, request.fields()));
    }
}
