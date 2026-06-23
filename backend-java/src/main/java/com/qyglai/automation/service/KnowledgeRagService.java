package com.qyglai.automation.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qyglai.automation.dto.KnowledgeAnswer;
import com.qyglai.automation.dto.KnowledgeDocumentIndexRequest;
import com.qyglai.automation.dto.KnowledgeIndexResult;
import com.qyglai.automation.dto.KnowledgeQueryRequest;
import com.qyglai.automation.dto.KnowledgeSearchHit;
import com.qyglai.automation.dto.KnowledgeSpaceCreateRequest;
import com.qyglai.automation.entity.KbChunkEntity;
import com.qyglai.automation.entity.KbDocumentEntity;
import com.qyglai.automation.entity.KbSpaceEntity;
import com.qyglai.automation.mapper.KbChunkMapper;
import com.qyglai.automation.mapper.KbDocumentMapper;
import com.qyglai.automation.mapper.KbSpaceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Java 知识库 RAG 服务，负责切片、向量化、检索、引用和答案生成。
 *
 * <p>MySQL保存知识元数据，豆包Embedding生成真实语义向量，Milvus负责COSINE Top-K检索。
 * 外部向量链路暂时不可用时，保留Java哈希向量作为降级检索能力。</p>
 */
@Service
public class KnowledgeRagService {

    private static final int VECTOR_DIMENSION = 256;
    private static final int CHUNK_SIZE = 700;
    private static final int CHUNK_OVERLAP = 100;
    private static final int DEFAULT_TOP_K = 5;
    private static final String FALLBACK_EMBEDDING_MODEL = "java-hash-embedding-v1";

    private final KbSpaceMapper spaceMapper;
    private final KbDocumentMapper documentMapper;
    private final KbChunkMapper chunkMapper;
    private final JavaAiModelGateway modelGateway;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;
    private final AiCallLogService aiCallLogService;
    private final DoubaoEmbeddingService embeddingService;
    private final MilvusVectorStoreService milvusVectorStoreService;
    private final DataPermissionService dataPermissionService;

    public KnowledgeRagService(KbSpaceMapper spaceMapper, KbDocumentMapper documentMapper,
                               KbChunkMapper chunkMapper, JavaAiModelGateway modelGateway,
                               AuditService auditService, ObjectMapper objectMapper,
                               AiCallLogService aiCallLogService, DoubaoEmbeddingService embeddingService,
                               MilvusVectorStoreService milvusVectorStoreService,
                               DataPermissionService dataPermissionService) {
        this.spaceMapper = spaceMapper;
        this.documentMapper = documentMapper;
        this.chunkMapper = chunkMapper;
        this.modelGateway = modelGateway;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
        this.aiCallLogService = aiCallLogService;
        this.embeddingService = embeddingService;
        this.milvusVectorStoreService = milvusVectorStoreService;
        this.dataPermissionService = dataPermissionService;
    }

    /**
     * 创建知识库空间。
     */
    public KbSpaceEntity createSpace(KnowledgeSpaceCreateRequest request) {
        KbSpaceEntity space = new KbSpaceEntity();
        space.setId(IdWorker.getId());
        space.setSpaceCode(request.code() == null || request.code().isBlank()
                ? "KB-" + System.currentTimeMillis() : request.code());
        space.setSpaceName(request.name());
        space.setPermissionScope(request.scope() == null || request.scope().isBlank() ? "company" : request.scope());
        space.setOwnerOrgId(request.ownerOrgId());
        space.setStatus("enabled");
        spaceMapper.insert(space);
        auditService.record("KB_SPACE_CREATE", "创建知识库空间", "kb_space", space.getId());
        return space;
    }

    /**
     * 将纯文本切片并建立向量索引。
     */
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeIndexResult indexText(KnowledgeDocumentIndexRequest request) {
        return indexDocument(request.spaceId(), null, request.title(), "text", request.sourceUrl(), request.content());
    }

    /**
     * 将已解析文件切片并建立向量索引。
     */
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeIndexResult indexFile(Long spaceId, Long fileId, String title, String content) {
        return indexDocument(spaceId, fileId, title, "file", null, content);
    }

    private KnowledgeIndexResult indexDocument(Long spaceId, Long fileId, String title, String docType,
                                               String sourceUrl, String content) {
        requireSpace(spaceId);
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("文档没有可索引的文本内容");
        }
        // 先保存文档主记录并标记为索引中，避免前端把尚未完成向量化的文档用于问答。
        KbDocumentEntity document = new KbDocumentEntity();
        document.setId(IdWorker.getId());
        document.setSpaceId(spaceId);
        document.setFileId(fileId);
        document.setTitle(title == null || title.isBlank() ? "未命名文档" : title);
        document.setDocType(docType);
        document.setSourceUrl(sourceUrl);
        document.setVersionNo("v1");
        document.setIndexingStatus("indexing");
        document.setStatus("enabled");
        documentMapper.insert(document);

        List<String> chunks = split(content);
        for (int i = 0; i < chunks.size(); i++) {
            // 每个切片独立生成向量；真实向量服务失败时，indexVector 会自动保留本地降级向量。
            KbChunkEntity chunk = new KbChunkEntity();
            chunk.setId(IdWorker.getId());
            chunk.setDocumentId(document.getId());
            chunk.setChunkIndex(i);
            chunk.setContent(chunks.get(i));
            chunk.setTokenCount(Math.max(1, chunks.get(i).length() / 2));
            indexVector(chunk, document);
            chunk.setCreatedAt(LocalDateTime.now());
            chunkMapper.insert(chunk);
        }
        document.setIndexingStatus("indexed");
        documentMapper.updateById(document);
        auditService.record("KB_DOCUMENT_INDEX", "知识库文档建立索引", "kb_document", document.getId());
        return new KnowledgeIndexResult(document.getId(), document.getTitle(), chunks.size(), document.getIndexingStatus());
    }

    /**
     * 对知识库执行向量相似度检索。
     */
    public List<KnowledgeSearchHit> search(String question, String scope, int topK) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("问题不能为空");
        }
        Map<Long, KbDocumentEntity> documents = new LinkedHashMap<>();
        for (KbDocumentEntity document : listDocuments(scope)) {
            documents.put(document.getId(), document);
        }
        if (documents.isEmpty()) return List.of();

        try {
            // 主链路使用豆包 Embedding + Milvus，保证语义召回能力。
            return searchMilvus(question, documents, topK);
        } catch (RuntimeException exception) {
            // 外部模型或 Milvus 不可用时使用 MySQL 中保存的本地哈希向量，保证知识问答仍可运行。
            return searchFallback(question, documents, topK);
        }
    }

    /**
     * 将问题转换为真实语义向量，在 Milvus 中召回候选切片，再按当前知识空间权限过滤。
     */
    private List<KnowledgeSearchHit> searchMilvus(String question, Map<Long, KbDocumentEntity> documents, int topK) {
        List<Double> queryVector = embeddingService.embed(question);
        // 扩大初始召回数量，为后续权限过滤和低分过滤留下足够候选。
        List<MilvusVectorStoreService.VectorHit> vectorHits = milvusVectorStoreService.search(
                queryVector, Math.max(20, Math.min(topK * 8, 100)));
        Map<Long, KbChunkEntity> chunks = new LinkedHashMap<>();
        chunkMapper.selectBatchIds(vectorHits.stream().map(MilvusVectorStoreService.VectorHit::chunkId).toList())
                .forEach(chunk -> chunks.put(chunk.getId(), chunk));
        return vectorHits.stream()
                .filter(hit -> documents.containsKey(hit.documentId()))
                .map(hit -> {
                    KbChunkEntity chunk = chunks.get(hit.chunkId());
                    KbDocumentEntity document = documents.get(hit.documentId());
                    return chunk == null ? null : new KnowledgeSearchHit(chunk.getId(), document.getId(),
                            document.getTitle(), chunk.getContent(), round(hit.score()), document.getSourceUrl());
                })
                .filter(hit -> hit != null && hit.score() > 0.05)
                .limit(Math.max(1, Math.min(topK, 20)))
                .toList();
    }

    /**
     * 使用存储在 MySQL 元数据中的本地哈希向量进行降级检索。
     */
    private List<KnowledgeSearchHit> searchFallback(String question, Map<Long, KbDocumentEntity> documents, int topK) {
        double[] queryVector = embedFallback(question);
        return chunkMapper.selectList(new LambdaQueryWrapper<KbChunkEntity>()
                        .in(KbChunkEntity::getDocumentId, documents.keySet()))
                .stream()
                .map(chunk -> toHit(chunk, documents.get(chunk.getDocumentId()), queryVector))
                .filter(hit -> hit.score() > 0.05)
                .sorted(Comparator.comparingDouble(KnowledgeSearchHit::score).reversed())
                .limit(Math.max(1, Math.min(topK, 20)))
                .toList();
    }

    /** 将历史知识切片重新生成真实Embedding并写入Milvus。 */
    public int reindexVectors() {
        List<KbChunkEntity> chunks = chunkMapper.selectList(new LambdaQueryWrapper<KbChunkEntity>().orderByAsc(KbChunkEntity::getId));
        // 重建集合可确保 Milvus 向量维度、索引参数与页面中最新配置保持一致。
        milvusVectorStoreService.recreateCollection();
        int indexed = 0;
        for (KbChunkEntity chunk : chunks) {
            KbDocumentEntity document = documentMapper.selectById(chunk.getDocumentId());
            if (document == null) continue;
            boolean realVectorIndexed = indexVector(chunk, document);
            chunkMapper.updateById(chunk);
            if (realVectorIndexed) indexed++;
        }
        auditService.record("KB_VECTOR_REINDEX", "知识库真实向量索引重建", "kb_chunk", null);
        return indexed;
    }

    public com.qyglai.automation.dto.KnowledgeVectorStatus vectorStatus() {
        return milvusVectorStoreService.status();
    }

    /**
     * 检索知识切片并基于引用上下文生成回答。
     */
    public KnowledgeAnswer query(KnowledgeQueryRequest request) {
        List<KnowledgeSearchHit> hits = search(request.question(), request.scope(), DEFAULT_TOP_K);
        if (hits.isEmpty()) {
            auditService.record("KB_QUERY_EMPTY", "知识库问答未命中", "kb_space", null);
            return new KnowledgeAnswer("知识库中暂未找到足够相关的资料，请补充文档或转人工确认。",
                    0.0, List.of(), true);
        }

        StringBuilder context = new StringBuilder();
        Set<String> citations = new LinkedHashSet<>();
        // 将召回片段编号后拼入提示词，同时把编号作为最终答案的可追溯引用返回前端。
        for (int i = 0; i < hits.size(); i++) {
            KnowledgeSearchHit hit = hits.get(i);
            String citation = "[" + (i + 1) + "] " + hit.title();
            citations.add(citation);
            context.append(citation).append("\n").append(hit.content()).append("\n\n");
        }
        String fallback = "根据知识库资料：" + hits.getFirst().content();
        String requestText = "问题：" + request.question() + "\n\n知识片段：\n" + context;
        long start = System.currentTimeMillis();
        // 大模型只负责基于召回资料组织答案；检索结果为空或模型失败时不会让模型自由编造。
        String answer = modelGateway.generateText("knowledge_query",
                "你是企业知识库助手。只能根据提供的知识片段回答，不得编造。"
                        + "回答中的关键结论必须使用[1]、[2]形式标注来源；资料不足时明确说明。",
                requestText,
                fallback
        );
        boolean modelSuccess = "success".equals(modelGateway.status().lastCallStatus());
        aiCallLogService.record("knowledge_query",
                request.scope() == null || request.scope().isBlank() ? "company" : request.scope(),
                hits.getFirst().documentId(),
                modelSuccess ? modelGateway.status().model() : "fallback-local-retrieval",
                "java-direct/kb/query", requestText, answer, start, modelSuccess,
                modelSuccess ? null : modelGateway.status().lastFallbackReason());
        double confidence = hits.stream().mapToDouble(KnowledgeSearchHit::score).average().orElse(0.0);
        auditService.record("KB_QUERY", "知识库RAG问答", "kb_space", null);
        return new KnowledgeAnswer(answer, round(confidence), List.copyOf(citations), confidence < 0.18);
    }

    public List<KbSpaceEntity> listSpaces() {
        return listSpaces(null, true);
    }

    public List<KbSpaceEntity> listSpaces(Long userId, boolean viewAll) {
        LambdaQueryWrapper<KbSpaceEntity> query = new LambdaQueryWrapper<KbSpaceEntity>()
                .eq(KbSpaceEntity::getStatus, "enabled").orderByDesc(KbSpaceEntity::getCreatedAt);
        dataPermissionService.applyOrgScope(query, KbSpaceEntity::getOwnerOrgId, userId, viewAll);
        return spaceMapper.selectList(query);
    }

    public List<KbDocumentEntity> listDocuments(String scope) {
        return listDocuments(scope, null, true);
    }

    public List<KbDocumentEntity> listDocuments(String scope, Long userId, boolean viewAll) {
        LambdaQueryWrapper<KbSpaceEntity> spaceQuery = new LambdaQueryWrapper<KbSpaceEntity>()
                        .eq(KbSpaceEntity::getStatus, "enabled")
                        .eq(scope != null && !scope.isBlank(),
                                KbSpaceEntity::getPermissionScope, scope);
        dataPermissionService.applyOrgScope(spaceQuery, KbSpaceEntity::getOwnerOrgId, userId, viewAll);
        List<Long> spaceIds = spaceMapper.selectList(spaceQuery).stream().map(KbSpaceEntity::getId).toList();
        if (spaceIds.isEmpty()) return List.of();
        return documentMapper.selectList(new LambdaQueryWrapper<KbDocumentEntity>()
                .in(KbDocumentEntity::getSpaceId, spaceIds)
                .eq(KbDocumentEntity::getStatus, "enabled")
                .eq(KbDocumentEntity::getIndexingStatus, "indexed")
                .orderByDesc(KbDocumentEntity::getCreatedAt));
    }

    private void requireSpace(Long spaceId) {
        KbSpaceEntity space = spaceMapper.selectById(spaceId);
        if (space == null || !"enabled".equals(space.getStatus())) {
            throw new IllegalArgumentException("知识库空间不存在或未启用: " + spaceId);
        }
    }

    private KnowledgeSearchHit toHit(KbChunkEntity chunk, KbDocumentEntity document, double[] queryVector) {
        double score = cosine(queryVector, vector(chunk.getMetadataJson()));
        return new KnowledgeSearchHit(chunk.getId(), document.getId(), document.getTitle(),
                chunk.getContent(), round(score), document.getSourceUrl());
    }

    private List<String> split(String raw) {
        String text = raw.replace("\r\n", "\n").replaceAll("\\n{3,}", "\n\n").strip();
        List<String> result = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(text.length(), start + CHUNK_SIZE);
            if (end < text.length()) {
                // 优先在段落或句号处截断，减少一个完整语义被拆到两个切片中的情况。
                int paragraph = text.lastIndexOf("\n", end);
                int sentence = Math.max(text.lastIndexOf('。', end), text.lastIndexOf('.', end));
                int boundary = Math.max(paragraph, sentence);
                if (boundary > start + CHUNK_SIZE / 2) end = boundary + 1;
            }
            String chunk = text.substring(start, end).strip();
            if (!chunk.isBlank()) result.add(chunk);
            if (end >= text.length()) break;
            // 保留固定重叠区，降低答案刚好位于切片边界时的召回损失。
            start = Math.max(start + 1, end - CHUNK_OVERLAP);
        }
        return result;
    }

    /**
     * 为单个知识切片建立真实向量索引；异常时写入本地向量元数据以支持降级检索。
     */
    private boolean indexVector(KbChunkEntity chunk, KbDocumentEntity document) {
        try {
            // 豆包负责生成真实语义向量，Milvus 只负责保存和相似度检索。
            List<Double> vector = embeddingService.embed(chunk.getContent());
            milvusVectorStoreService.upsert(chunk.getId(), document.getId(), document.getSpaceId(), vector);
            chunk.setEmbeddingModel(embeddingService.model());
            chunk.setVectorRef("milvus:" + milvusVectorStoreService.status().collection() + ":" + chunk.getId());
            chunk.setMetadataJson(metadata(embedFallback(chunk.getContent()), document));
            return true;
        } catch (RuntimeException exception) {
            // 索引任务不因外部服务短暂故障整体回滚，后续可通过“重建向量索引”补齐。
            chunk.setEmbeddingModel(FALLBACK_EMBEDDING_MODEL);
            chunk.setVectorRef("mysql:" + chunk.getId());
            chunk.setMetadataJson(metadata(embedFallback(chunk.getContent()), document));
            return false;
        }
    }

    /**
     * 生成无需外部依赖的归一化哈希向量，仅用于真实向量服务不可用时兜底。
     */
    private double[] embedFallback(String text) {
        double[] vector = new double[VECTOR_DIMENSION];
        String normalized = text.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        for (String token : tokens(normalized)) {
            int hash = token.hashCode();
            int index = Math.floorMod(hash, VECTOR_DIMENSION);
            vector[index] += (hash & 1) == 0 ? 1.0 : -1.0;
        }
        double norm = Math.sqrt(Arrays.stream(vector).map(value -> value * value).sum());
        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) vector[i] /= norm;
        }
        return vector;
    }

    private List<String> tokens(String text) {
        List<String> tokens = new ArrayList<>(Arrays.asList(text.split("[^\\p{L}\\p{N}]+")));
        String compact = text.replaceAll("[^\\p{IsHan}]", "");
        for (int i = 0; i < compact.length() - 1; i++) {
            tokens.add(compact.substring(i, i + 2));
        }
        return tokens.stream().filter(token -> !token.isBlank()).toList();
    }

    private double cosine(double[] left, double[] right) {
        if (left.length != right.length) return 0.0;
        double value = 0.0;
        for (int i = 0; i < left.length; i++) value += left[i] * right[i];
        return Math.max(0.0, value);
    }

    private String metadata(double[] vector, KbDocumentEntity document) {
        try {
            Map<String, Object> metadata = new LinkedHashMap<>();
            if (vector != null) metadata.put("vector", vector);
            metadata.put("title", document.getTitle());
            metadata.put("spaceId", document.getSpaceId());
            metadata.put("fallbackEmbeddingModel", FALLBACK_EMBEDDING_MODEL);
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("知识向量序列化失败", ex);
        }
    }

    private double[] vector(String metadataJson) {
        try {
            Map<String, Object> metadata = objectMapper.readValue(metadataJson, new TypeReference<>() {});
            List<?> values = (List<?>) metadata.get("vector");
            if (values == null) return new double[0];
            double[] vector = new double[values.size()];
            for (int i = 0; i < values.size(); i++) vector[i] = ((Number) values.get(i)).doubleValue();
            return vector;
        } catch (Exception ex) {
            return new double[0];
        }
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
