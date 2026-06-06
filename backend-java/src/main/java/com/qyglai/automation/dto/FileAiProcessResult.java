package com.qyglai.automation.dto;

import com.qyglai.automation.entity.DocParseResultEntity;
import com.qyglai.automation.entity.FileAssetEntity;

/**
 * 文件AI处理闭环结果。
 *
 * @param file 文件资产
 * @param parseResult 文档解析结果
 * @param extraction AI抽取结果
 * @param businessRecord 生成的业务记录
 * @param reviewTask 生成的复核任务
 * @param workflowInstance 自动发起的审批流程实例
 */
public record FileAiProcessResult(
        FileAssetEntity file,
        DocParseResultEntity parseResult,
        ExtractionResult extraction,
        Object businessRecord,
        Object reviewTask,
        WorkflowInstanceSummary workflowInstance
) {
}
