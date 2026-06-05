package com.qyglai.automation.service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qyglai.automation.dto.SimpleCreateRequest;
import com.qyglai.automation.entity.AiEvaluationSampleEntity;
import com.qyglai.automation.entity.AiModelProviderEntity;
import com.qyglai.automation.entity.AiPromptTemplateEntity;
import com.qyglai.automation.mapper.AiEvaluationSampleMapper;
import com.qyglai.automation.mapper.AiModelProviderMapper;
import com.qyglai.automation.mapper.AiPromptTemplateMapper;
import org.springframework.stereotype.Service;

/**
 * AI 治理服务，负责模型供应商、提示词模板和评测样本管理。
 */
@Service
public class AiGovernanceService {

    private final AiModelProviderMapper providerMapper;
    private final AiPromptTemplateMapper promptTemplateMapper;
    private final AiEvaluationSampleMapper evaluationSampleMapper;

    public AiGovernanceService(AiModelProviderMapper providerMapper, AiPromptTemplateMapper promptTemplateMapper,
                               AiEvaluationSampleMapper evaluationSampleMapper) {
        this.providerMapper = providerMapper;
        this.promptTemplateMapper = promptTemplateMapper;
        this.evaluationSampleMapper = evaluationSampleMapper;
    }

    /**
     * 查询模型供应商。
     *
     * @return 模型供应商列表
     */
    public List<AiModelProviderEntity> listProviders() {
        return providerMapper.selectList(new LambdaQueryWrapper<AiModelProviderEntity>().orderByDesc(AiModelProviderEntity::getCreatedAt));
    }

    /**
     * 创建提示词模板。
     *
     * @param request 创建请求
     * @return 提示词模板实体
     */
    public AiPromptTemplateEntity createPromptTemplate(SimpleCreateRequest request) {
        AiPromptTemplateEntity template = new AiPromptTemplateEntity();
        template.setTemplateCode(request.code() == null ? "PROMPT-" + System.currentTimeMillis() : request.code());
        template.setScenario(request.type() == null ? "general" : request.type());
        template.setVersionNo(1);
        template.setUserPromptTemplate(request.content() == null ? "请处理以下企业流程数据：{input}" : request.content());
        template.setStatus("enabled");
        promptTemplateMapper.insert(template);
        return template;
    }

    /**
     * 查询提示词模板。
     *
     * @return 提示词模板列表
     */
    public List<AiPromptTemplateEntity> listPromptTemplates() {
        return promptTemplateMapper.selectList(new LambdaQueryWrapper<AiPromptTemplateEntity>().orderByDesc(AiPromptTemplateEntity::getCreatedAt));
    }

    /**
     * 查询评测样本。
     *
     * @return 评测样本列表
     */
    public List<AiEvaluationSampleEntity> listEvaluationSamples() {
        return evaluationSampleMapper.selectList(new LambdaQueryWrapper<AiEvaluationSampleEntity>().orderByDesc(AiEvaluationSampleEntity::getCreatedAt));
    }
}

