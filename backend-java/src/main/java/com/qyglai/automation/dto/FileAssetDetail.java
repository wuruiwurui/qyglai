package com.qyglai.automation.dto;

import java.util.List;

import com.qyglai.automation.entity.ContractRecordEntity;
import com.qyglai.automation.entity.DocParseResultEntity;
import com.qyglai.automation.entity.FileAssetEntity;
import com.qyglai.automation.entity.InvoiceRecordEntity;
import com.qyglai.automation.entity.ReviewTaskEntity;
import com.qyglai.automation.entity.WorkflowInstanceEntity;

/**
 * 文件资产详情，聚合文件、解析结果、业务台账和复核任务。
 */
public record FileAssetDetail(
        /**
         * 文件资产信息。
         */
        FileAssetEntity file,
        /**
         * 最近一次文档解析结果。
         */
        DocParseResultEntity parseResult,
        /**
         * 文件生成的合同记录。
         */
        ContractRecordEntity contract,
        /**
         * 文件生成的发票记录。
         */
        InvoiceRecordEntity invoice,
        /**
         * 文件生成或关联的复核任务。
         */
        List<ReviewTaskEntity> reviewTasks,
        /**
         * 关联业务记录的审批流程实例。
         */
        List<WorkflowInstanceEntity> workflowInstances) {
}
