package com.qyglai.automation.service;

import com.qyglai.automation.entity.ContractRecordEntity;
import com.qyglai.automation.entity.FileAssetEntity;
import com.qyglai.automation.entity.InvoiceRecordEntity;
import com.qyglai.automation.entity.WorkflowInstanceEntity;
import com.qyglai.automation.mapper.ContractRecordMapper;
import com.qyglai.automation.mapper.FileAssetMapper;
import com.qyglai.automation.mapper.InvoiceRecordMapper;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * 审批流程与合同、发票业务状态之间的同步服务。
 */
@Service
public class BusinessApprovalStatusService {

    private final ContractRecordMapper contractMapper;
    private final InvoiceRecordMapper invoiceMapper;
    private final FileAssetMapper fileAssetMapper;
    private final AuditService auditService;
    private final BusinessEventService businessEventService;

    public BusinessApprovalStatusService(ContractRecordMapper contractMapper,
                                         InvoiceRecordMapper invoiceMapper,
                                         FileAssetMapper fileAssetMapper,
                                         AuditService auditService,
                                         BusinessEventService businessEventService) {
        this.contractMapper = contractMapper;
        this.invoiceMapper = invoiceMapper;
        this.fileAssetMapper = fileAssetMapper;
        this.auditService = auditService;
        this.businessEventService = businessEventService;
    }

    /** 流程发起后将业务记录标记为审批中。 */
    public void markPending(String businessType, Long businessId) {
        updateStatus(businessType, businessId, "pending_approval", false);
    }

    /** 流程最终通过后同步业务和文件状态。 */
    public void markApproved(WorkflowInstanceEntity instance) {
        updateStatus(instance.getBusinessType(), instance.getBusinessId(), "approved", false);
        auditService.record("BUSINESS_APPROVAL_APPROVED", "业务审批通过", instance.getBusinessType(), instance.getBusinessId());
        businessEventService.publish("business.approval_approved", instance.getBusinessId(),
                Map.of("businessType", instance.getBusinessType(), "instanceId", instance.getId()));
    }

    /** 流程驳回后同步业务状态，并将原文件送回字段修正入口。 */
    public void markRejected(WorkflowInstanceEntity instance) {
        updateStatus(instance.getBusinessType(), instance.getBusinessId(), "rejected", true);
        auditService.record("BUSINESS_APPROVAL_REJECTED", "业务审批驳回待修正", instance.getBusinessType(), instance.getBusinessId());
        businessEventService.publish("business.approval_rejected", instance.getBusinessId(),
                Map.of("businessType", instance.getBusinessType(), "instanceId", instance.getId()));
    }

    private void updateStatus(String businessType, Long businessId, String status, boolean correctionRequired) {
        if (businessType == null || businessId == null) {
            return;
        }
        if ("contract".equalsIgnoreCase(businessType) || "contract_record".equalsIgnoreCase(businessType)) {
            ContractRecordEntity contract = contractMapper.selectById(businessId);
            if (contract != null) {
                contract.setReviewStatus(status);
                contract.setUpdatedAt(LocalDateTime.now());
                contractMapper.updateById(contract);
                updateFile(contract.getFileId(), correctionRequired);
            }
            return;
        }
        if ("invoice".equalsIgnoreCase(businessType) || "invoice_record".equalsIgnoreCase(businessType)) {
            InvoiceRecordEntity invoice = invoiceMapper.selectById(businessId);
            if (invoice != null) {
                invoice.setVerifyStatus(status);
                invoice.setUpdatedAt(LocalDateTime.now());
                invoiceMapper.updateById(invoice);
                updateFile(invoice.getFileId(), correctionRequired);
            }
        }
    }

    private void updateFile(Long fileId, boolean correctionRequired) {
        if (fileId == null) {
            return;
        }
        FileAssetEntity file = fileAssetMapper.selectById(fileId);
        if (file != null) {
            file.setParseStatus(correctionRequired ? "correction_required" : "completed");
            file.setUpdatedAt(LocalDateTime.now());
            fileAssetMapper.updateById(file);
        }
    }
}
