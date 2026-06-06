package com.qyglai.automation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qyglai.automation.dto.FileAssetDetail;
import com.qyglai.automation.entity.FieldCorrectionHistoryEntity;
import com.qyglai.automation.mapper.FieldCorrectionHistoryMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 文件字段修正与审计服务。
 */
@Service
public class FieldCorrectionService {

    private static final Map<String, String> FIELD_NAMES = Map.ofEntries(
            Map.entry("scenario", "业务场景"), Map.entry("amount", "价税合计"),
            Map.entry("line_amount", "不含税金额"), Map.entry("total_amount", "价税合计"),
            Map.entry("tax_amount", "税额"), Map.entry("tax_rate", "税率"),
            Map.entry("invoice_no", "发票号码"), Map.entry("invoice_code", "发票代码"),
            Map.entry("invoice_date", "开票日期"), Map.entry("buyer_name", "购买方名称"),
            Map.entry("buyer_tax_no", "购买方税号"), Map.entry("seller_name", "销售方名称"),
            Map.entry("seller_tax_no", "销售方税号"), Map.entry("contract_no", "合同编号"),
            Map.entry("party_a", "甲方"), Map.entry("party_b", "乙方"),
            Map.entry("payment_terms", "付款条款"), Map.entry("risk_level", "风险等级"));

    private final AutomationWorkspaceService workspaceService;
    private final FieldCorrectionHistoryMapper historyMapper;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public FieldCorrectionService(AutomationWorkspaceService workspaceService,
                                  FieldCorrectionHistoryMapper historyMapper,
                                  AuditService auditService,
                                  ObjectMapper objectMapper) {
        this.workspaceService = workspaceService;
        this.historyMapper = historyMapper;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    /**
     * 修正抽取字段，并记录逐字段历史和审计快照。
     */
    @Transactional(rollbackFor = Exception.class)
    public FileAssetDetail correct(Long fileId, Map<String, String> fields, String reason,
                                   Long operatorUserId, String operatorName) {
        FileAssetDetail beforeDetail = workspaceService.getFileDetail(fileId);
        Map<String, String> beforeFields = extractFields(beforeDetail);
        Map<String, String> afterFields = new LinkedHashMap<>(beforeFields);
        if (fields != null) {
            afterFields.putAll(fields);
        }
        Map<String, Map<String, String>> changes = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : afterFields.entrySet()) {
            String oldValue = normalized(beforeFields.get(entry.getKey()));
            String newValue = normalized(entry.getValue());
            if (!Objects.equals(oldValue, newValue)) {
                changes.put(entry.getKey(), Map.of("oldValue", oldValue, "newValue", newValue));
            }
        }
        if (changes.isEmpty()) {
            throw new IllegalArgumentException("没有字段发生变化，无需保存");
        }

        FileAssetDetail result = workspaceService.confirmFileFields(fileId, afterFields);
        Long batchId = IdWorker.getId();
        Long businessId = result.invoice() != null ? result.invoice().getId()
                : result.contract() != null ? result.contract().getId() : null;
        for (Map.Entry<String, Map<String, String>> change : changes.entrySet()) {
            FieldCorrectionHistoryEntity history = new FieldCorrectionHistoryEntity();
            history.setBatchId(batchId);
            history.setFileId(fileId);
            history.setBusinessType(result.file().getBusinessType());
            history.setBusinessId(businessId);
            history.setFieldKey(change.getKey());
            history.setFieldName(FIELD_NAMES.getOrDefault(change.getKey(), change.getKey()));
            history.setOldValue(change.getValue().get("oldValue"));
            history.setNewValue(change.getValue().get("newValue"));
            history.setReason(reason);
            history.setOperatorUserId(operatorUserId);
            history.setOperatorName(operatorName);
            historyMapper.insert(history);
        }
        auditService.recordDetailed(operatorUserId, operatorName, "FILE_FIELD_CORRECTION", "修正文件抽取字段",
                "file_asset", fileId, toJson(Map.of("fields", beforeFields)),
                toJson(Map.of("fields", afterFields, "reason", reason, "batchId", String.valueOf(batchId))));
        return result;
    }

    /**
     * 查询指定文件的字段修正历史。
     */
    public List<FieldCorrectionHistoryEntity> listByFile(Long fileId) {
        return historyMapper.selectList(new LambdaQueryWrapper<FieldCorrectionHistoryEntity>()
                .eq(FieldCorrectionHistoryEntity::getFileId, fileId)
                .orderByDesc(FieldCorrectionHistoryEntity::getCreatedAt)
                .orderByDesc(FieldCorrectionHistoryEntity::getId));
    }

    private Map<String, String> extractFields(FileAssetDetail detail) {
        Map<String, String> result = new LinkedHashMap<>();
        if (detail.parseResult() == null || detail.parseResult().getLayoutJson() == null) {
            return result;
        }
        try {
            JsonNode root = objectMapper.readTree(detail.parseResult().getLayoutJson());
            JsonNode fields = root.path("extraction").path("fields");
            if (!fields.isObject()) {
                fields = root.path("fields");
            }
            fields.fields().forEachRemaining(entry -> result.put(entry.getKey(), entry.getValue().asText("")));
            return result;
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("原字段数据格式错误", ex);
        }
    }

    private String normalized(String value) {
        return value == null ? "" : value.trim();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("审计快照序列化失败", ex);
        }
    }
}
