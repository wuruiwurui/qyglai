package com.qyglai.automation.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.qyglai.automation.dto.SimpleCreateRequest;
import com.qyglai.automation.entity.ReconciliationBatchEntity;
import com.qyglai.automation.entity.ReconciliationItemEntity;
import com.qyglai.automation.mapper.ReconciliationBatchMapper;
import com.qyglai.automation.mapper.ReconciliationItemMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 财务对账服务，负责对账批次与差异明细管理。
 */
@Service
public class ReconciliationService {

    private final ReconciliationBatchMapper batchMapper;
    private final ReconciliationItemMapper itemMapper;
    private final AuditService auditService;
    private final AiBusinessApplicationService aiBusinessApplicationService;
    private final DataPermissionService dataPermissionService;

    public ReconciliationService(ReconciliationBatchMapper batchMapper, ReconciliationItemMapper itemMapper,
                                 AuditService auditService, AiBusinessApplicationService aiBusinessApplicationService,
                                 DataPermissionService dataPermissionService) {
        this.batchMapper = batchMapper;
        this.itemMapper = itemMapper;
        this.auditService = auditService;
        this.aiBusinessApplicationService = aiBusinessApplicationService;
        this.dataPermissionService = dataPermissionService;
    }

    /**
     * 创建对账批次并生成一条示例差异明细。
     *
     * @param request 创建请求
     * @return 对账批次实体
     */
    @Transactional(rollbackFor = Exception.class)
    public ReconciliationBatchEntity createBatch(SimpleCreateRequest request) {
        return createBatch(request, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReconciliationBatchEntity createBatch(SimpleCreateRequest request, Long ownerUserId) {
        BigDecimal amount = request.amount() == null ? BigDecimal.ZERO : request.amount();
        ReconciliationBatchEntity batch = new ReconciliationBatchEntity();
        batch.setBatchNo(request.code() == null ? "REC-" + IdWorker.getId() : request.code());
        batch.setSupplierName(request.name() == null ? "示例供应商" : request.name());
        batch.setPeriodStart(LocalDate.now().withDayOfMonth(1));
        batch.setPeriodEnd(LocalDate.now());
        batch.setExpectedAmount(amount);
        batch.setActualAmount(amount);
        batch.setDiffAmount(BigDecimal.ZERO);
        batch.setOwnerUserId(ownerUserId);
        batch.setStatus("pending");
        batchMapper.insert(batch);
        AiBusinessApplicationService.ReconciliationDecision analysis =
                aiBusinessApplicationService.explainReconciliation(batch.getSupplierName(), amount, amount, BigDecimal.ZERO);

        ReconciliationItemEntity item = new ReconciliationItemEntity();
        item.setBatchId(batch.getId());
        item.setItemType("invoice");
        item.setSourceNo(batch.getBatchNo());
        item.setExpectedAmount(amount);
        item.setActualAmount(amount);
        item.setDiffAmount(BigDecimal.ZERO);
        item.setDiffReason(analysis.summary());
        item.setStatus(analysis.status());
        itemMapper.insert(item);
        auditService.record("RECONCILIATION_CREATE", "创建对账批次", "reconciliation_batch", batch.getId());
        return batch;
    }

    /**
     * 查询对账批次。
     *
     * @return 对账批次列表
     */
    public List<ReconciliationBatchEntity> listBatches() {
        return listBatches(null, true);
    }

    public List<ReconciliationBatchEntity> listBatches(Long userId, boolean viewAll) {
        LambdaQueryWrapper<ReconciliationBatchEntity> query = new LambdaQueryWrapper<ReconciliationBatchEntity>().orderByDesc(ReconciliationBatchEntity::getCreatedAt);
        dataPermissionService.applyOwnerScope(query, ReconciliationBatchEntity::getOwnerUserId, userId, viewAll);
        return batchMapper.selectList(query);
    }

    /**
     * 查询对账明细。
     *
     * @return 对账明细列表
     */
    public List<ReconciliationItemEntity> listItems() {
        return listItems(null, true);
    }

    public List<ReconciliationItemEntity> listItems(Long userId, boolean viewAll) {
        List<Long> batchIds = listBatches(userId, viewAll).stream().map(ReconciliationBatchEntity::getId).toList();
        if (batchIds.isEmpty()) return List.of();
        return itemMapper.selectList(new LambdaQueryWrapper<ReconciliationItemEntity>()
                .in(ReconciliationItemEntity::getBatchId, batchIds)
                .orderByDesc(ReconciliationItemEntity::getCreatedAt));
    }
}
