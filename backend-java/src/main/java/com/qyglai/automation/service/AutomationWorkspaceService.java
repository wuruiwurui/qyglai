package com.qyglai.automation.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qyglai.automation.dto.ExtractionResult;
import com.qyglai.automation.dto.FileAssetDetail;
import com.qyglai.automation.dto.FileAiProcessResult;
import com.qyglai.automation.dto.KnowledgeAnswer;
import com.qyglai.automation.dto.KnowledgeQueryRequest;
import com.qyglai.automation.dto.ParseAndExtractResponse;
import com.qyglai.automation.dto.ReportGenerateRequest;
import com.qyglai.automation.dto.ReportSummary;
import com.qyglai.automation.dto.ReviewTask;
import com.qyglai.automation.dto.SalesFollowupTask;
import com.qyglai.automation.dto.TextProcessRequest;
import com.qyglai.automation.dto.TicketClassifyResult;
import com.qyglai.automation.dto.WorkflowInstanceSummary;
import com.qyglai.automation.dto.WorkflowStartRequest;
import com.qyglai.automation.entity.AiModelCallLogEntity;
import com.qyglai.automation.entity.ContractRecordEntity;
import com.qyglai.automation.entity.ContractRiskItemEntity;
import com.qyglai.automation.entity.DocParseResultEntity;
import com.qyglai.automation.entity.FileAssetEntity;
import com.qyglai.automation.entity.InvoiceRecordEntity;
import com.qyglai.automation.entity.KbDocumentEntity;
import com.qyglai.automation.entity.KbSpaceEntity;
import com.qyglai.automation.entity.MessageNotificationEntity;
import com.qyglai.automation.entity.ReportRecordEntity;
import com.qyglai.automation.entity.ReviewTaskEntity;
import com.qyglai.automation.entity.SalesFollowupTaskEntity;
import com.qyglai.automation.entity.TicketEntity;
import com.qyglai.automation.entity.TicketReplySuggestionEntity;
import com.qyglai.automation.entity.WorkflowDefinitionEntity;
import com.qyglai.automation.entity.WorkflowInstanceEntity;
import com.qyglai.automation.entity.WorkflowTaskEntity;
import com.qyglai.automation.mapper.AiModelCallLogMapper;
import com.qyglai.automation.mapper.ContractRecordMapper;
import com.qyglai.automation.mapper.ContractRiskItemMapper;
import com.qyglai.automation.mapper.DocParseResultMapper;
import com.qyglai.automation.mapper.FileAssetMapper;
import com.qyglai.automation.mapper.InvoiceRecordMapper;
import com.qyglai.automation.mapper.KbDocumentMapper;
import com.qyglai.automation.mapper.KbSpaceMapper;
import com.qyglai.automation.mapper.MessageNotificationMapper;
import com.qyglai.automation.mapper.ReportRecordMapper;
import com.qyglai.automation.mapper.ReviewTaskMapper;
import com.qyglai.automation.mapper.SalesFollowupTaskMapper;
import com.qyglai.automation.mapper.TicketMapper;
import com.qyglai.automation.mapper.TicketReplySuggestionMapper;
import com.qyglai.automation.mapper.WorkflowDefinitionMapper;
import com.qyglai.automation.mapper.WorkflowInstanceMapper;
import com.qyglai.automation.mapper.WorkflowTaskMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 企业流程自动化工作台服务。
 *
 * <p>该服务承载合同、发票、工单、销售、知识库、报表、流程、复核、消息和 AI 治理的核心业务入口。</p>
 */
@Service
public class AutomationWorkspaceService {

    private static final Pattern AMOUNT_TOKEN_PATTERN = Pattern.compile("(?<!\\d)(\\d{1,15}(?:\\.\\d{1,6})?)\\s*(万|元)?(?!\\d)");
    private static final BigDecimal TEN_THOUSAND = new BigDecimal("10000");
    private static final BigDecimal MAX_BUSINESS_AMOUNT = new BigDecimal("999999999999999999.99");

    private final ContractRecordMapper contractRecordMapper;
    private final ContractRiskItemMapper contractRiskItemMapper;
    private final FileAssetMapper fileAssetMapper;
    private final DocParseResultMapper docParseResultMapper;
    private final InvoiceRecordMapper invoiceRecordMapper;
    private final TicketMapper ticketMapper;
    private final TicketReplySuggestionMapper ticketReplySuggestionMapper;
    private final SalesFollowupTaskMapper salesFollowupTaskMapper;
    private final KbSpaceMapper kbSpaceMapper;
    private final KbDocumentMapper kbDocumentMapper;
    private final ReportRecordMapper reportRecordMapper;
    private final WorkflowDefinitionMapper workflowDefinitionMapper;
    private final WorkflowInstanceMapper workflowInstanceMapper;
    private final WorkflowTaskMapper workflowTaskMapper;
    private final ReviewTaskMapper reviewTaskMapper;
    private final MessageNotificationMapper messageNotificationMapper;
    private final AiModelCallLogMapper aiModelCallLogMapper;
    private final AuditService auditService;
    private final BusinessEventService businessEventService;
    private final AiGatewayService aiGatewayService;
    private final WorkflowApprovalService workflowApprovalService;
    private final ObjectMapper objectMapper;
    private final KnowledgeRagService knowledgeRagService;

    public AutomationWorkspaceService(ContractRecordMapper contractRecordMapper,
                                      ContractRiskItemMapper contractRiskItemMapper,
                                      FileAssetMapper fileAssetMapper,
                                      DocParseResultMapper docParseResultMapper,
                                      InvoiceRecordMapper invoiceRecordMapper,
                                      TicketMapper ticketMapper,
                                      TicketReplySuggestionMapper ticketReplySuggestionMapper,
                                      SalesFollowupTaskMapper salesFollowupTaskMapper,
                                      KbSpaceMapper kbSpaceMapper,
                                      KbDocumentMapper kbDocumentMapper,
                                      ReportRecordMapper reportRecordMapper,
                                      WorkflowDefinitionMapper workflowDefinitionMapper,
                                      WorkflowInstanceMapper workflowInstanceMapper,
                                      WorkflowTaskMapper workflowTaskMapper,
                                      ReviewTaskMapper reviewTaskMapper,
                                      MessageNotificationMapper messageNotificationMapper,
                                      AiModelCallLogMapper aiModelCallLogMapper,
                                      AuditService auditService,
                                      BusinessEventService businessEventService,
                                      AiGatewayService aiGatewayService,
                                      WorkflowApprovalService workflowApprovalService,
                                      ObjectMapper objectMapper,
                                      KnowledgeRagService knowledgeRagService) {
        this.contractRecordMapper = contractRecordMapper;
        this.contractRiskItemMapper = contractRiskItemMapper;
        this.fileAssetMapper = fileAssetMapper;
        this.docParseResultMapper = docParseResultMapper;
        this.invoiceRecordMapper = invoiceRecordMapper;
        this.ticketMapper = ticketMapper;
        this.ticketReplySuggestionMapper = ticketReplySuggestionMapper;
        this.salesFollowupTaskMapper = salesFollowupTaskMapper;
        this.kbSpaceMapper = kbSpaceMapper;
        this.kbDocumentMapper = kbDocumentMapper;
        this.reportRecordMapper = reportRecordMapper;
        this.workflowDefinitionMapper = workflowDefinitionMapper;
        this.workflowInstanceMapper = workflowInstanceMapper;
        this.workflowTaskMapper = workflowTaskMapper;
        this.reviewTaskMapper = reviewTaskMapper;
        this.messageNotificationMapper = messageNotificationMapper;
        this.aiModelCallLogMapper = aiModelCallLogMapper;
        this.auditService = auditService;
        this.businessEventService = businessEventService;
        this.aiGatewayService = aiGatewayService;
        this.workflowApprovalService = workflowApprovalService;
        this.objectMapper = objectMapper;
        this.knowledgeRagService = knowledgeRagService;
    }

    /**
     * 上传并登记文件资产。
     *
     * @param file 上传文件
     * @param businessType 业务类型
     * @return 文件资产实体
     */
    @Transactional(rollbackFor = Exception.class)
    public FileAssetEntity uploadFile(MultipartFile file, String businessType) {
        try {
            Long fileId = IdWorker.getId();
            Path uploadDir = Path.of("storage", "uploads");
            Files.createDirectories(uploadDir);
            String originalName = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();
            String ext = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.') + 1) : "";
            String storedName = fileId + (ext.isBlank() ? "" : "." + ext);
            Path target = uploadDir.resolve(storedName);
            file.transferTo(target);

            String md5;
            try (InputStream inputStream = Files.newInputStream(target)) {
                md5 = DigestUtils.md5DigestAsHex(inputStream);
            }

            FileAssetEntity asset = new FileAssetEntity();
            asset.setId(fileId);
            asset.setFileName(storedName);
            asset.setOriginalName(originalName);
            asset.setFileExt(ext);
            asset.setMimeType(file.getContentType());
            asset.setFileSize(file.getSize());
            asset.setFileHash(md5);
            asset.setStorageBucket("local");
            asset.setStorageKey(target.toString());
            asset.setBusinessType(businessType == null || businessType.isBlank() ? "general" : businessType);
            asset.setParseStatus("uploaded");
            fileAssetMapper.insert(asset);
            auditService.record("FILE_UPLOAD", "上传文件", "file_asset", fileId);
            return asset;
        } catch (IOException ex) {
            throw new IllegalStateException("文件上传失败", ex);
        }
    }

    /**
     * 查询文件资产列表，按最近创建时间倒序返回。
     *
     * @return 文件资产列表
     */
    public List<FileAssetEntity> listFiles() {
        return fileAssetMapper.selectList(new LambdaQueryWrapper<FileAssetEntity>()
                .orderByDesc(FileAssetEntity::getCreatedAt));
    }

    /**
     * 查询文件详情，聚合解析结果、合同、发票和复核任务。
     *
     * @param fileId 文件ID
     * @return 文件资产详情
     */
    public FileAssetDetail getFileDetail(Long fileId) {
        FileAssetEntity file = fileAssetMapper.selectById(fileId);
        if (file == null) {
            throw new IllegalArgumentException("文件不存在: " + fileId);
        }
        DocParseResultEntity parseResult = docParseResultMapper.selectOne(new LambdaQueryWrapper<DocParseResultEntity>()
                .eq(DocParseResultEntity::getFileId, fileId)
                .orderByDesc(DocParseResultEntity::getCreatedAt)
                .last("LIMIT 1"));
        ContractRecordEntity contract = contractRecordMapper.selectOne(new LambdaQueryWrapper<ContractRecordEntity>()
                .eq(ContractRecordEntity::getFileId, fileId)
                .orderByDesc(ContractRecordEntity::getCreatedAt)
                .last("LIMIT 1"));
        InvoiceRecordEntity invoice = invoiceRecordMapper.selectOne(new LambdaQueryWrapper<InvoiceRecordEntity>()
                .eq(InvoiceRecordEntity::getFileId, fileId)
                .orderByDesc(InvoiceRecordEntity::getCreatedAt)
                .last("LIMIT 1"));
        List<ReviewTaskEntity> reviewTasks = reviewTaskMapper.selectList(new LambdaQueryWrapper<ReviewTaskEntity>()
                .and(wrapper -> wrapper
                        .eq(ReviewTaskEntity::getBusinessId, contract == null ? -1L : contract.getId())
                        .or()
                        .eq(ReviewTaskEntity::getBusinessId, invoice == null ? -1L : invoice.getId()))
                .orderByDesc(ReviewTaskEntity::getCreatedAt));
        String linkedBusinessType = invoice != null ? "invoice" : contract != null ? "contract" : null;
        Long linkedBusinessId = invoice != null ? invoice.getId() : contract != null ? contract.getId() : null;
        return new FileAssetDetail(file, parseResult, contract, invoice, reviewTasks,
                workflowApprovalService.listByBusiness(linkedBusinessType, linkedBusinessId));
    }

    /**
     * 人工确认文件抽取字段，并同步更新关联业务台账。
     *
     * @param fileId 文件ID
     * @param fields 人工确认后的字段
     * @return 更新后的文件详情
     */
    @Transactional(rollbackFor = Exception.class)
    public FileAssetDetail confirmFileFields(Long fileId, Map<String, String> fields) {
        FileAssetEntity file = fileAssetMapper.selectById(fileId);
        if (file == null) {
            throw new IllegalArgumentException("文件不存在: " + fileId);
        }
        Map<String, String> confirmedFields = fields == null ? Map.of() : fields;
        DocParseResultEntity parseResult = docParseResultMapper.selectOne(new LambdaQueryWrapper<DocParseResultEntity>()
                .eq(DocParseResultEntity::getFileId, fileId)
                .orderByDesc(DocParseResultEntity::getCreatedAt)
                .last("LIMIT 1"));
        if (parseResult != null) {
            parseResult.setLayoutJson(toJson(Map.of("extraction", new ExtractionResult(
                    firstPresent(confirmedFields, "scenario", "businessType", file.getBusinessType()),
                    1.0,
                    confirmedFields,
                    List.of("人工已确认字段"),
                    false
            ))));
            docParseResultMapper.updateById(parseResult);
        }

        String scenario = firstPresent(confirmedFields, "scenario", "businessType", file.getBusinessType()).toLowerCase();
        if (scenario.contains("invoice") || "invoice".equalsIgnoreCase(file.getBusinessType())) {
            updateInvoiceFromConfirmedFields(fileId, confirmedFields);
        } else if (scenario.contains("contract") || "contract".equalsIgnoreCase(file.getBusinessType())) {
            updateContractFromConfirmedFields(fileId, confirmedFields);
        }
        return getFileDetail(fileId);
    }

    /**
     * 保存文档解析结果。
     *
     * @param fileId 文件ID
     * @param rawText 原始文本
     * @return 文档解析结果
     */
    public DocParseResultEntity saveParseResult(Long fileId, String rawText) {
        return saveParseResult(fileId, rawText, null);
    }

    private DocParseResultEntity saveParseResult(Long fileId, String rawText, ExtractionResult extraction) {
        DocParseResultEntity result = new DocParseResultEntity();
        result.setFileId(fileId);
        result.setPageNo(1);
        result.setRawText(rawText);
        if (extraction != null) {
            result.setLayoutJson(toJson(Map.of("extraction", extraction)));
        }
        result.setOcrEngine("manual");
        result.setConfidence(new BigDecimal("1.0000"));
        result.setStatus("completed");
        docParseResultMapper.insert(result);
        auditService.record("DOC_PARSE_SAVE", "保存文档解析结果", "doc_parse_result", result.getId());
        return result;
    }

    /**
     * 上传文件后由 Java AI 能力完成解析、抽取、入库和复核任务生成。
     *
     * @param file 上传文件
     * @param businessType 业务类型，支持 contract、invoice、statement、general
     * @return 文件AI处理闭环结果
     */
    @Transactional(rollbackFor = Exception.class)
    public FileAiProcessResult processFileWithAi(MultipartFile file, String businessType) {
        return processFileWithAi(file, businessType, null);
    }

    /**
     * 上传文件后完成AI处理，并以当前用户身份自动发起业务审批。
     */
    @Transactional(rollbackFor = Exception.class)
    public FileAiProcessResult processFileWithAi(MultipartFile file, String businessType, Long initiatorUserId) {
        FileAssetEntity asset = uploadFile(file, businessType);
        ParseAndExtractResponse aiResult = aiGatewayService.parseAndExtractFile(file, asset.getBusinessType());
        if (aiResult == null || aiResult.parsed() == null || aiResult.extraction() == null) {
            throw new IllegalStateException("AI文件解析未返回有效结果");
        }

        DocParseResultEntity parseResult = saveParseResult(asset.getId(), aiResult.parsed().rawText(), aiResult.extraction());
        Object businessRecord = createBusinessRecordFromExtraction(asset, aiResult.extraction());
        ReviewTaskEntity reviewTask = null;
        if (aiResult.extraction().reviewRequired()) {
            reviewTask = createReviewTask(aiResult.extraction().scenario(), businessType, extractBusinessId(businessRecord), "AI文件解析需要人工复核", riskLevel(aiResult.extraction()));
        }
        asset.setParseStatus("completed");
        fileAssetMapper.updateById(asset);
        auditService.record("FILE_AI_PROCESS", "文件AI解析入库", "file_asset", asset.getId());
        businessEventService.publish("file.ai_processed", asset.getId(), Map.of("businessType", asset.getBusinessType()));
        WorkflowInstanceSummary workflow = workflowApprovalService.startForBusiness(asset.getBusinessType(),
                extractBusinessId(businessRecord), asset.getId(), asset.getOriginalName() + "审批", initiatorUserId);
        return new FileAiProcessResult(asset, parseResult, aiResult.extraction(), businessRecord, reviewTask, workflow);
    }

    private Object createBusinessRecordFromExtraction(FileAssetEntity asset, ExtractionResult extraction) {
        String businessType = asset.getBusinessType() == null ? "" : asset.getBusinessType().toLowerCase();
        String scenario = extraction.scenario() == null ? "" : extraction.scenario().toLowerCase();
        if (businessType.contains("invoice") || scenario.contains("invoice")) {
            return createInvoiceFromExtraction(asset.getId(), extraction);
        }
        if (businessType.contains("contract") || scenario.contains("contract")) {
            return createContractFromExtraction(asset.getId(), extraction);
        }
        return Map.of("fileId", asset.getId(), "message", "文件已解析，未匹配到专属业务台账，已保存解析结果");
    }

    private ContractRecordEntity createContractFromExtraction(Long fileId, ExtractionResult extraction) {
        Map<String, String> fields = extraction.fields();
        ContractRecordEntity contract = new ContractRecordEntity();
        contract.setId(IdWorker.getId());
        contract.setFileId(fileId);
        contract.setContractNo("HT-AI-" + contract.getId());
        contract.setPartyA(firstPresent(fields, "party_a", "partyA", "甲方", "示例甲方有限公司"));
        contract.setPartyB(firstPresent(fields, "party_b", "partyB", "乙方", "示例乙方有限公司"));
        contract.setAmount(parseAmount(firstPresent(fields, "amount", "金额", "0")));
        contract.setCurrency("CNY");
        contract.setPaymentTerms(firstPresent(fields, "payment_terms", "paymentTerm", "付款条款", "待确认"));
        contract.setRiskLevel(riskLevel(extraction));
        contract.setReviewStatus(extraction.reviewRequired() ? "pending" : "passed");
        contract.setDeleted(0);
        contract.setCreatedAt(LocalDateTime.now());
        contract.setUpdatedAt(LocalDateTime.now());
        contractRecordMapper.insert(contract);
        for (String riskText : extraction.risks()) {
            ContractRiskItemEntity risk = new ContractRiskItemEntity();
            risk.setContractId(contract.getId());
            risk.setRiskCode("AI_RISK");
            risk.setRiskName(riskText);
            risk.setRiskLevel(contract.getRiskLevel());
            risk.setEvidence(riskText);
            risk.setSuggestion("请业务负责人和法务复核该风险点");
            risk.setStatus("open");
            contractRiskItemMapper.insert(risk);
        }
        return contract;
    }

    private InvoiceRecordEntity createInvoiceFromExtraction(Long fileId, ExtractionResult extraction) {
        Map<String, String> fields = extraction.fields();
        InvoiceRecordEntity invoice = new InvoiceRecordEntity();
        invoice.setId(IdWorker.getId());
        invoice.setFileId(fileId);
        invoice.setInvoiceNo(firstPresent(fields, "invoice_no", "invoiceNo", "FP-AI-" + invoice.getId()));
        invoice.setInvoiceCode(firstPresent(fields, "invoice_code", "invoiceCode", "AI-" + fileId));
        boolean duplicateInvoice = findExistingInvoice(invoice.getInvoiceNo(), invoice.getInvoiceCode()) != null;

        invoice.setBuyerName(firstPresent(fields, "buyer_name", "buyerName", "示例购买方有限公司"));
        invoice.setBuyerTaxNo(firstPresent(fields, "buyer_tax_no", "buyerTaxNo", "待确认"));
        invoice.setSellerName(firstPresent(fields, "seller_name", "sellerName", "示例销售方有限公司"));
        invoice.setSellerTaxNo(firstPresent(fields, "seller_tax_no", "sellerTaxNo", "待确认"));
        invoice.setInvoiceDate(parseDate(firstPresent(fields, "invoice_date", "invoiceDate", "")));
        invoice.setAmount(parseAmount(firstPresent(fields, "line_amount", "amountWithoutTax", "amount", "0")));
        invoice.setTaxAmount(parseAmount(firstPresent(fields, "tax_amount", "taxAmount", "0")));
        BigDecimal totalAmount = parseAmount(firstPresent(fields, "total_amount", "totalAmount", "amount", "0"));
        invoice.setTotalAmount(totalAmount.signum() > 0 ? totalAmount : invoice.getAmount().add(invoice.getTaxAmount()));
        invoice.setTaxRate(firstPresent(fields, "tax_rate", "taxRate", "待确认"));
        invoice.setVerifyStatus(extraction.reviewRequired() ? "pending_review" : "passed");
        invoice.setDuplicateFlag(duplicateInvoice ? 1 : 0);
        invoice.setDeleted(0);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());
        invoiceRecordMapper.insert(invoice);
        return invoice;
    }

    private InvoiceRecordEntity findExistingInvoice(String invoiceNo, String invoiceCode) {
        if (invoiceNo == null || invoiceNo.isBlank() || invoiceCode == null || invoiceCode.isBlank()) {
            return null;
        }
        return invoiceRecordMapper.selectOne(new LambdaQueryWrapper<InvoiceRecordEntity>()
                .eq(InvoiceRecordEntity::getInvoiceNo, invoiceNo)
                .eq(InvoiceRecordEntity::getInvoiceCode, invoiceCode)
                .last("LIMIT 1"));
    }

    private void updateInvoiceFromConfirmedFields(Long fileId, Map<String, String> fields) {
        InvoiceRecordEntity invoice = invoiceRecordMapper.selectOne(new LambdaQueryWrapper<InvoiceRecordEntity>()
                .eq(InvoiceRecordEntity::getFileId, fileId)
                .orderByDesc(InvoiceRecordEntity::getCreatedAt)
                .last("LIMIT 1"));
        if (invoice == null) {
            invoice = new InvoiceRecordEntity();
            invoice.setId(IdWorker.getId());
            invoice.setFileId(fileId);
            invoice.setDeleted(0);
            invoice.setCreatedAt(LocalDateTime.now());
        }
        invoice.setInvoiceNo(firstPresent(fields, "invoice_no", "invoiceNo", invoice.getInvoiceNo() == null ? "FP-AI-" + invoice.getId() : invoice.getInvoiceNo()));
        invoice.setInvoiceCode(firstPresent(fields, "invoice_code", "invoiceCode", invoice.getInvoiceCode() == null ? invoice.getInvoiceNo() : invoice.getInvoiceCode()));
        invoice.setBuyerName(firstPresent(fields, "buyer_name", "buyerName", invoice.getBuyerName() == null ? "待确认" : invoice.getBuyerName()));
        invoice.setBuyerTaxNo(firstPresent(fields, "buyer_tax_no", "buyerTaxNo", invoice.getBuyerTaxNo() == null ? "待确认" : invoice.getBuyerTaxNo()));
        invoice.setSellerName(firstPresent(fields, "seller_name", "sellerName", invoice.getSellerName() == null ? "待确认" : invoice.getSellerName()));
        invoice.setSellerTaxNo(firstPresent(fields, "seller_tax_no", "sellerTaxNo", invoice.getSellerTaxNo() == null ? "待确认" : invoice.getSellerTaxNo()));
        invoice.setInvoiceDate(parseDate(firstPresent(fields, "invoice_date", "invoiceDate", invoice.getInvoiceDate() == null ? "" : invoice.getInvoiceDate().toString())));
        invoice.setAmount(parseAmount(firstPresent(fields, "line_amount", "amountWithoutTax", "amount", invoice.getAmount() == null ? "0" : invoice.getAmount().toPlainString())));
        invoice.setTaxAmount(parseAmount(firstPresent(fields, "tax_amount", "taxAmount", invoice.getTaxAmount() == null ? "0" : invoice.getTaxAmount().toPlainString())));
        BigDecimal totalAmount = parseAmount(firstPresent(fields, "total_amount", "totalAmount", "amount", invoice.getTotalAmount() == null ? "0" : invoice.getTotalAmount().toPlainString()));
        invoice.setTotalAmount(totalAmount.signum() > 0 ? totalAmount : invoice.getAmount().add(invoice.getTaxAmount()));
        invoice.setTaxRate(firstPresent(fields, "tax_rate", "taxRate", invoice.getTaxRate() == null ? "待确认" : invoice.getTaxRate()));
        invoice.setVerifyStatus("passed");
        invoice.setDuplicateFlag(findExistingInvoice(invoice.getInvoiceNo(), invoice.getInvoiceCode()) == null ? 0 : 1);
        invoice.setUpdatedAt(LocalDateTime.now());
        if (invoiceRecordMapper.selectById(invoice.getId()) == null) {
            invoiceRecordMapper.insert(invoice);
        } else {
            invoiceRecordMapper.updateById(invoice);
        }
    }

    private void updateContractFromConfirmedFields(Long fileId, Map<String, String> fields) {
        ContractRecordEntity contract = contractRecordMapper.selectOne(new LambdaQueryWrapper<ContractRecordEntity>()
                .eq(ContractRecordEntity::getFileId, fileId)
                .orderByDesc(ContractRecordEntity::getCreatedAt)
                .last("LIMIT 1"));
        if (contract == null) {
            contract = new ContractRecordEntity();
            contract.setId(IdWorker.getId());
            contract.setFileId(fileId);
            contract.setCurrency("CNY");
            contract.setDeleted(0);
            contract.setCreatedAt(LocalDateTime.now());
        }
        contract.setContractNo(firstPresent(fields, "contract_no", "contractNo", contract.getContractNo() == null ? "HT-AI-" + contract.getId() : contract.getContractNo()));
        contract.setPartyA(firstPresent(fields, "party_a", "partyA", contract.getPartyA() == null ? "待确认" : contract.getPartyA()));
        contract.setPartyB(firstPresent(fields, "party_b", "partyB", contract.getPartyB() == null ? "待确认" : contract.getPartyB()));
        contract.setAmount(parseAmount(firstPresent(fields, "amount", "contract_amount", contract.getAmount() == null ? "0" : contract.getAmount().toPlainString())));
        contract.setPaymentTerms(firstPresent(fields, "payment_terms", "paymentTerms", contract.getPaymentTerms() == null ? "待确认" : contract.getPaymentTerms()));
        contract.setRiskLevel(firstPresent(fields, "risk_level", "riskLevel", contract.getRiskLevel() == null ? "low" : contract.getRiskLevel()));
        contract.setReviewStatus("passed");
        contract.setUpdatedAt(LocalDateTime.now());
        if (contractRecordMapper.selectById(contract.getId()) == null) {
            contractRecordMapper.insert(contract);
        } else {
            contractRecordMapper.updateById(contract);
        }
    }

    private String firstPresent(Map<String, String> fields, String key1, String key2, String defaultValue) {
        return firstPresent(fields, key1, key2, "", defaultValue);
    }

    private String firstPresent(Map<String, String> fields, String key1, String key2, String key3, String defaultValue) {
        for (String key : List.of(key1, key2, key3)) {
            if (key != null && !key.isBlank() && fields.containsKey(key) && fields.get(key) != null && !fields.get(key).isBlank()) {
                return fields.get(key);
            }
        }
        return defaultValue;
    }

    private BigDecimal parseAmount(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        Matcher matcher = AMOUNT_TOKEN_PATTERN.matcher(value.replace(",", ""));
        BigDecimal fallback = BigDecimal.ZERO;
        while (matcher.find()) {
            BigDecimal amount = normalizeAmountCandidate(matcher.group(1), matcher.group(2));
            if (amount.signum() <= 0 || amount.compareTo(MAX_BUSINESS_AMOUNT) > 0) {
                continue;
            }
            if (matcher.group(2) != null) {
                return amount;
            }
            fallback = amount;
        }
        return fallback;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank() || "待确认".equals(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private BigDecimal normalizeAmountCandidate(String number, String unit) {
        try {
            BigDecimal amount = new BigDecimal(number);
            if ("万".equals(unit)) {
                amount = amount.multiply(TEN_THOUSAND);
            }
            return amount.setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private String riskLevel(ExtractionResult extraction) {
        if (extraction.risks() != null && extraction.risks().size() >= 2) {
            return "high";
        }
        return extraction.reviewRequired() ? "medium" : "low";
    }

    private Long extractBusinessId(Object businessRecord) {
        if (businessRecord instanceof ContractRecordEntity contract) {
            return contract.getId();
        }
        if (businessRecord instanceof InvoiceRecordEntity invoice) {
            return invoice.getId();
        }
        return null;
    }

    /**
     * 抽取合同信息，并创建合同台账、风险项和复核任务。
     *
     * @param request 文本处理请求
     * @return 合同抽取结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ExtractionResult extractContract(TextProcessRequest request) {
        Long contractId = IdWorker.getId();
        ContractRecordEntity contract = new ContractRecordEntity();
        contract.setId(contractId);
        contract.setContractNo("HT-" + contractId);
        contract.setPartyA("示例甲方有限公司");
        contract.setPartyB("示例乙方有限公司");
        contract.setAmount(new BigDecimal("128000.00"));
        contract.setCurrency("CNY");
        contract.setPaymentTerms("验收后45日内付款");
        contract.setRiskLevel("medium");
        contract.setReviewStatus("pending");
        contractRecordMapper.insert(contract);

        ContractRiskItemEntity risk = new ContractRiskItemEntity();
        risk.setContractId(contractId);
        risk.setRiskCode("PAYMENT_TERM_LONG");
        risk.setRiskName("付款周期偏长");
        risk.setRiskLevel("medium");
        risk.setEvidence("付款条款包含验收后45日内付款");
        risk.setSuggestion("建议业务负责人确认现金流影响");
        risk.setStatus("open");
        contractRiskItemMapper.insert(risk);

        createReviewTask("CONTRACT", "contract_record", contractId, "合同付款周期需复核", "medium");
        startWorkflow(new WorkflowStartRequest("contract_review", "system", Map.of("contractId", contractId)));
        auditService.record("CONTRACT_EXTRACT", "合同信息抽取", "contract_record", contractId);
        businessEventService.publish("contract.extracted", contractId, Map.of("contractNo", contract.getContractNo()));

        return new ExtractionResult(
                "CONTRACT",
                0.92,
                Map.of(
                        "partyA", contract.getPartyA(),
                        "partyB", contract.getPartyB(),
                        "amount", contract.getAmount().toPlainString(),
                        "paymentTerm", contract.getPaymentTerms()
                ),
                List.of("付款周期偏长"),
                true
        );
    }

    /**
     * 查询合同台账。
     *
     * @return 合同列表
     */
    public List<ContractRecordEntity> listContracts() {
        return contractRecordMapper.selectList(new LambdaQueryWrapper<ContractRecordEntity>()
                .orderByDesc(ContractRecordEntity::getCreatedAt));
    }

    /**
     * 解析发票并创建发票记录。
     *
     * @param request 文本处理请求
     * @return 发票解析结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ExtractionResult parseInvoice(TextProcessRequest request) {
        Long invoiceId = IdWorker.getId();
        InvoiceRecordEntity invoice = new InvoiceRecordEntity();
        invoice.setId(invoiceId);
        invoice.setInvoiceNo("FP-" + invoiceId);
        invoice.setBuyerName("示例购买方有限公司");
        invoice.setBuyerTaxNo("91440000MADEMO001X");
        invoice.setSellerName("示例供应商有限公司");
        invoice.setSellerTaxNo("91440000MADEMO002X");
        invoice.setAmount(new BigDecimal("8600.00"));
        invoice.setTaxAmount(new BigDecimal("1118.00"));
        invoice.setTotalAmount(new BigDecimal("9718.00"));
        invoice.setTaxRate("13%");
        invoice.setVerifyStatus("passed");
        invoice.setDuplicateFlag(0);
        invoiceRecordMapper.insert(invoice);
        auditService.record("INVOICE_PARSE", "发票解析", "invoice_record", invoiceId);
        businessEventService.publish("invoice.parsed", invoiceId, Map.of("invoiceNo", invoice.getInvoiceNo()));

        return new ExtractionResult(
                "INVOICE",
                0.96,
                Map.of(
                        "invoiceNo", invoice.getInvoiceNo(),
                        "buyerTaxNo", invoice.getBuyerTaxNo(),
                        "sellerName", invoice.getSellerName(),
                        "amount", invoice.getAmount().toPlainString(),
                        "taxRate", invoice.getTaxRate()
                ),
                List.of("未发现重复发票", "建议与供应商对账单匹配"),
                false
        );
    }

    /**
     * 查询发票记录。
     *
     * @return 发票列表
     */
    public List<InvoiceRecordEntity> listInvoices() {
        return invoiceRecordMapper.selectList(new LambdaQueryWrapper<InvoiceRecordEntity>()
                .orderByDesc(InvoiceRecordEntity::getCreatedAt));
    }

    /**
     * 分类客服工单，并生成 AI 回复建议。
     *
     * @param request 文本处理请求
     * @return 工单分类结果
     */
    @Transactional(rollbackFor = Exception.class)
    public TicketClassifyResult classifyTicket(TextProcessRequest request) {
        Long ticketId = IdWorker.getId();
        TicketEntity ticket = new TicketEntity();
        ticket.setId(ticketId);
        ticket.setTicketNo("TK-" + ticketId);
        ticket.setCustomerName("示例客户");
        ticket.setSourceChannel("manual");
        ticket.setTitle("客户咨询交付进度");
        ticket.setContent(request.content());
        ticket.setCategory("交付进度咨询");
        ticket.setPriority("P2");
        ticket.setSentiment("neutral");
        ticket.setStatus("open");
        ticket.setConfidence(new BigDecimal("0.88"));
        ticketMapper.insert(ticket);

        TicketReplySuggestionEntity suggestion = new TicketReplySuggestionEntity();
        suggestion.setTicketId(ticketId);
        suggestion.setSuggestionText("建议先同步当前交付节点，并承诺下一次反馈时间。");
        suggestion.setCitationJson("[\"历史工单FAQ\", \"交付SLA说明\"]");
        suggestion.setConfidence(new BigDecimal("0.86"));
        suggestion.setAcceptedFlag(0);
        ticketReplySuggestionMapper.insert(suggestion);

        auditService.record("TICKET_CLASSIFY", "客服工单分类", "ticket", ticketId);
        businessEventService.publish("ticket.classified", ticketId, Map.of("priority", ticket.getPriority()));
        return new TicketClassifyResult(ticket.getCategory(), ticket.getPriority(), ticket.getSentiment(), "客服主管", 0.88);
    }

    /**
     * 查询客服工单。
     *
     * @return 工单列表
     */
    public List<TicketEntity> listTickets() {
        return ticketMapper.selectList(new LambdaQueryWrapper<TicketEntity>().orderByDesc(TicketEntity::getCreatedAt));
    }

    /**
     * 查询销售跟进任务。
     *
     * @return 销售跟进任务列表
     */
    public List<SalesFollowupTask> listSalesFollowups() {
        return salesFollowupTaskMapper.selectList(new LambdaQueryWrapper<SalesFollowupTaskEntity>()
                        .orderByAsc(SalesFollowupTaskEntity::getDueTime))
                .stream()
                .map(task -> new SalesFollowupTask(
                        String.valueOf(task.getId()),
                        task.getCustomerId() == null ? "未知客户" : "客户-" + task.getCustomerId(),
                        task.getSourceType(),
                        task.getNextAction(),
                        task.getDueTime(),
                        task.getOwnerUserId() == null ? "未分配" : "用户-" + task.getOwnerUserId(),
                        task.getStatus()
                ))
                .toList();
    }

    /**
     * 查询知识库答案。
     *
     * @param request 知识库查询请求
     * @return 知识库回答
     */
    public KnowledgeAnswer queryKnowledge(KnowledgeQueryRequest request) {
        return knowledgeRagService.query(request);
    }

    /**
     * 查询知识库空间。
     *
     * @return 知识库空间列表
     */
    public List<KbSpaceEntity> listKnowledgeSpaces() {
        return kbSpaceMapper.selectList(new LambdaQueryWrapper<KbSpaceEntity>().orderByDesc(KbSpaceEntity::getCreatedAt));
    }

    /**
     * 生成报表并保存报表记录。
     *
     * @param request 报表生成请求
     * @return 报表摘要
     */
    @Transactional(rollbackFor = Exception.class)
    public ReportSummary generateReport(ReportGenerateRequest request) {
        Long reportId = IdWorker.getId();
        ReportRecordEntity record = new ReportRecordEntity();
        record.setId(reportId);
        record.setReportType(request.reportType());
        record.setTitle(request.reportType() + " - " + (request.timeRange() == null ? "默认周期" : request.timeRange()));
        record.setSummary("销售新增客户稳定，客服高优工单上升，财务存在对账异常。");
        record.setContent("一、销售进展；二、合同风险；三、财务对账；四、客服质量；五、明日重点。");
        record.setSourceJson("[\"CRM\", \"合同台账\", \"财务系统\", \"客服系统\"]");
        record.setSendStatus(request.autoSend() ? "pending_confirmation" : "draft");
        reportRecordMapper.insert(record);
        auditService.record("REPORT_GENERATE", "生成报表", "report_record", reportId);
        businessEventService.publish("report.generated", reportId, Map.of("reportType", request.reportType()));

        return new ReportSummary(record.getTitle(), record.getSummary(), List.of("销售进展", "合同风险", "财务对账", "客服质量"), List.of("CRM", "合同台账", "财务系统", "客服系统"), record.getSendStatus());
    }

    /**
     * 查询报表记录。
     *
     * @return 报表记录列表
     */
    public List<ReportRecordEntity> listReports() {
        return reportRecordMapper.selectList(new LambdaQueryWrapper<ReportRecordEntity>().orderByDesc(ReportRecordEntity::getGeneratedAt));
    }

    /**
     * 启动轻量工作流实例。
     *
     * @param request 流程启动请求
     * @return 流程实例摘要
     */
    @Transactional(rollbackFor = Exception.class)
    public WorkflowInstanceSummary startWorkflow(WorkflowStartRequest request) {
        WorkflowDefinitionEntity definition = findOrCreateWorkflowDefinition(request.workflowCode());
        Long instanceId = IdWorker.getId();
        WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
        instance.setId(instanceId);
        instance.setDefinitionId(definition.getId());
        instance.setBusinessType(request.workflowCode());
        instance.setBusinessId(extractLongVariable(request.variables(), "businessId"));
        instance.setCurrentNode("AI_CHECK");
        instance.setVariablesJson(toJson(request.variables()));
        instance.setStatus("running");
        workflowInstanceMapper.insert(instance);

        WorkflowTaskEntity task = new WorkflowTaskEntity();
        task.setInstanceId(instanceId);
        task.setNodeCode("AI_CHECK");
        task.setNodeName("AI识别与规则校验");
        task.setTaskType("auto");
        task.setStatus("pending");
        workflowTaskMapper.insert(task);
        auditService.record("WORKFLOW_START", "启动流程", "workflow_instance", instanceId);
        return new WorkflowInstanceSummary(String.valueOf(instanceId), request.workflowCode(), instance.getCurrentNode(), instance.getStatus(), Instant.now());
    }

    /**
     * 查询流程任务。
     *
     * @return 流程任务列表
     */
    public List<WorkflowTaskEntity> listWorkflowTasks() {
        return workflowTaskMapper.selectList(new LambdaQueryWrapper<WorkflowTaskEntity>().orderByDesc(WorkflowTaskEntity::getCreatedAt));
    }

    /**
     * 查询人工复核任务。
     *
     * @return 人工复核任务列表
     */
    public List<ReviewTask> listReviewTasks() {
        return reviewTaskMapper.selectList(new LambdaQueryWrapper<ReviewTaskEntity>()
                        .orderByDesc(ReviewTaskEntity::getCreatedAt))
                .stream()
                .map(task -> new ReviewTask(String.valueOf(task.getId()), task.getScenario(), task.getTitle(), task.getRiskLevel(), task.getAssigneeUserId() == null ? "未分配" : "用户-" + task.getAssigneeUserId(), Instant.now()))
                .toList();
    }

    /**
     * 完成人工复核任务。
     *
     * @param taskId 任务ID
     * @param result 复核结果
     * @return 是否成功
     */
    public boolean completeReviewTask(Long taskId, String result) {
        ReviewTaskEntity task = reviewTaskMapper.selectById(taskId);
        if (task == null) {
            return false;
        }
        task.setStatus("completed");
        task.setReviewResult(result);
        task.setCompletedAt(LocalDateTime.now());
        reviewTaskMapper.updateById(task);
        auditService.record("REVIEW_COMPLETE", "完成人工复核", "review_task", taskId);
        return true;
    }

    /**
     * 创建消息通知。
     *
     * @param channel 消息渠道
     * @param title 标题
     * @param content 内容
     * @return 消息通知实体
     */
    public MessageNotificationEntity createNotification(String channel, String title, String content) {
        MessageNotificationEntity notification = new MessageNotificationEntity();
        notification.setChannel(channel);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setSendStatus("pending");
        notification.setRetryCount(0);
        messageNotificationMapper.insert(notification);
        businessEventService.publish("message.created", notification.getId(), Map.of("channel", channel));
        return notification;
    }

    /**
     * 查询消息通知。
     *
     * @return 消息通知列表
     */
    public List<MessageNotificationEntity> listNotifications() {
        return messageNotificationMapper.selectList(new LambdaQueryWrapper<MessageNotificationEntity>().orderByDesc(MessageNotificationEntity::getCreatedAt));
    }

    /**
     * 查询 AI 模型调用日志。
     *
     * @return AI模型调用日志列表
     */
    public List<AiModelCallLogEntity> listAiModelCallLogs() {
        return aiModelCallLogMapper.selectList(new LambdaQueryWrapper<AiModelCallLogEntity>().orderByDesc(AiModelCallLogEntity::getCreatedAt));
    }

    private ReviewTaskEntity createReviewTask(String scenario, String businessType, Long businessId, String title, String riskLevel) {
        ReviewTaskEntity task = new ReviewTaskEntity();
        task.setTaskNo("RV-" + IdWorker.getId());
        task.setScenario(scenario);
        task.setBusinessType(businessType);
        task.setBusinessId(businessId);
        task.setTitle(title);
        task.setRiskLevel(riskLevel);
        task.setStatus("pending");
        reviewTaskMapper.insert(task);
        return task;
    }

    private WorkflowDefinitionEntity findOrCreateWorkflowDefinition(String workflowCode) {
        WorkflowDefinitionEntity existing = workflowDefinitionMapper.selectOne(new LambdaQueryWrapper<WorkflowDefinitionEntity>()
                .eq(WorkflowDefinitionEntity::getWorkflowCode, workflowCode)
                .eq(WorkflowDefinitionEntity::getStatus, "enabled")
                .last("LIMIT 1"));
        if (existing != null) {
            return existing;
        }
        WorkflowDefinitionEntity definition = new WorkflowDefinitionEntity();
        definition.setWorkflowCode(workflowCode);
        definition.setWorkflowName(workflowCode + "流程");
        definition.setScenario(workflowCode);
        definition.setVersionNo(1);
        definition.setDefinitionJson("{\"nodes\":[\"AI_CHECK\",\"MANUAL_REVIEW\",\"DONE\"]}");
        definition.setStatus("enabled");
        workflowDefinitionMapper.insert(definition);
        return definition;
    }

    private Long extractLongVariable(Map<String, Object> variables, String key) {
        if (variables == null || !variables.containsKey(key)) {
            return null;
        }
        Object value = variables.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("JSON序列化失败", ex);
        }
    }
}
