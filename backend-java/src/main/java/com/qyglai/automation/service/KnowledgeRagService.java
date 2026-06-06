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
 * <p>当前使用 MySQL 保存确定性稀疏哈希向量，部署时无需额外向量数据库。
 * 后续可通过替换向量存储实现平滑迁移至 Milvus 或 OpenSearch。</p>
 */
@Service
public class KnowledgeRagService {

    private static final int VECTOR_DIMENSION = 256;
    private static final int CHUNK_SIZE = 700;
    private static final int CHUNK_OVERLAP = 100;
    private static final int DEFAULT_TOP_K = 5;
    private static final String EMBEDDING_MODEL = "java-hash-embedding-v1";

    private final KbSpaceMapper spaceMapper;
    private final KbDocumentMapper documentMapper;
    private final KbChunkMapper chunkMapper;
    private final JavaAiModelGateway modelGateway;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;
    private final AiCallLogService aiCallLogService;

    public KnowledgeRagService(KbSpaceMapper spaceMapper, KbDocumentMapper documentMapper,
                               KbChunkMapper chunkMapper, JavaAiModelGateway modelGateway,
                               AuditService auditService, ObjectMapper objectMapper,
                               AiCallLogService aiCallLogService) {
        this.spaceMapper = spaceMapper;
        this.documentMapper = documentMapper;
        this.chunkMapper = chunkMapper;
        this.modelGateway = modelGateway;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
        this.aiCallLogService = aiCallLogService;
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
            KbChunkEntity chunk = new KbChunkEntity();
            chunk.setId(IdWorker.getId());
            chunk.setDocumentId(document.getId());
            chunk.setChunkIndex(i);
            chunk.setContent(chunks.get(i));
            chunk.setTokenCount(Math.max(1, chunks.get(i).length() / 2));
            chunk.setEmbeddingModel(EMBEDDING_MODEL);
            chunk.setVectorRef("mysql:" + chunk.getId());
            chunk.setMetadataJson(metadata(embed(chunks.get(i)), document));
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

        double[] queryVector = embed(question);
        return chunkMapper.selectList(new LambdaQueryWrapper<KbChunkEntity>()
                        .in(KbChunkEntity::getDocumentId, documents.keySet()))
                .stream()
                .map(chunk -> toHit(chunk, documents.get(chunk.getDocumentId()), queryVector))
                .filter(hit -> hit.score() > 0.05)
                .sorted(Comparator.comparingDouble(KnowledgeSearchHit::score).reversed())
                .limit(Math.max(1, Math.min(topK, 20)))
                .toList();
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
        for (int i = 0; i < hits.size(); i++) {
            KnowledgeSearchHit hit = hits.get(i);
            String citation = "[" + (i + 1) + "] " + hit.title();
            citations.add(citation);
            context.append(citation).append("\n").append(hit.content()).append("\n\n");
        }
        String fallback = "根据知识库资料：" + hits.getFirst().content();
        String requestText = "问题：" + request.question() + "\n\n知识片段：\n" + context;
        long start = System.currentTimeMillis();
        String answer = modelGateway.generateText(
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
        return spaceMapper.selectList(new LambdaQueryWrapper<KbSpaceEntity>()
                .eq(KbSpaceEntity::getStatus, "enabled").orderByDesc(KbSpaceEntity::getCreatedAt));
    }

    public List<KbDocumentEntity> listDocuments(String scope) {
        List<Long> spaceIds = spaceMapper.selectList(new LambdaQueryWrapper<KbSpaceEntity>()
                        .eq(KbSpaceEntity::getStatus, "enabled")
                        .eq(scope != null && !scope.isBlank(),
                                KbSpaceEntity::getPermissionScope, scope))
                .stream().map(KbSpaceEntity::getId).toList();
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
                int paragraph = text.lastIndexOf("\n", end);
                int sentence = Math.max(text.lastIndexOf('。', end), text.lastIndexOf('.', end));
                int boundary = Math.max(paragraph, sentence);
                if (boundary > start + CHUNK_SIZE / 2) end = boundary + 1;
            }
            String chunk = text.substring(start, end).strip();
            if (!chunk.isBlank()) result.add(chunk);
            if (end >= text.length()) break;
            start = Math.max(start + 1, end - CHUNK_OVERLAP);
        }
        return result;
    }

    private double[] embed(String text) {
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
            return objectMapper.writeValueAsString(Map.of(
                    "vector", vector,
                    "title", document.getTitle(),
                    "spaceId", document.getSpaceId(),
                    "embeddingModel", EMBEDDING_MODEL
            ));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("知识向量序列化失败", ex);
        }
    }

    private double[] vector(String metadataJson) {
        try {
            Map<String, Object> metadata = objectMapper.readValue(metadataJson, new TypeReference<>() {});
            List<?> values = (List<?>) metadata.get("vector");
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
