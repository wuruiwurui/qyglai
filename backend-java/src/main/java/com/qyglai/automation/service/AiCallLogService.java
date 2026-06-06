package com.qyglai.automation.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.qyglai.automation.entity.AiModelCallLogEntity;
import com.qyglai.automation.mapper.AiModelCallLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * AI 模型调用日志服务。
 *
 * <p>日志写入失败不会影响实际业务调用。</p>
 */
@Service
public class AiCallLogService {

    private final AiModelCallLogMapper mapper;

    public AiCallLogService(AiModelCallLogMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 记录一次模型调用。
     *
     * @param scenario 业务场景
     * @param businessType 业务类型
     * @param businessId 关联业务ID
     * @param modelName 模型名称
     * @param promptTemplateCode 调用入口
     * @param requestText 请求文本
     * @param responseText 响应文本
     * @param startMs 调用开始时间戳
     * @param success 是否成功调用真实模型
     * @param errorMessage 失败或降级原因
     */
    public void record(String scenario, String businessType, Long businessId, String modelName,
                       String promptTemplateCode, String requestText, String responseText,
                       long startMs, boolean success, String errorMessage) {
        try {
            AiModelCallLogEntity log = new AiModelCallLogEntity();
            log.setId(IdWorker.getId());
            log.setScenario(scenario);
            log.setBusinessType(businessType);
            log.setBusinessId(businessId);
            log.setModelName(modelName);
            log.setPromptTemplateCode(promptTemplateCode);
            log.setRequestTokens(estimateTokens(requestText));
            log.setResponseTokens(estimateTokens(responseText));
            log.setCostAmount(BigDecimal.ZERO);
            log.setLatencyMs((int) Math.max(0, System.currentTimeMillis() - startMs));
            log.setSuccessFlag(success ? 1 : 0);
            log.setErrorMessage(truncate(errorMessage));
            log.setRequestHash(DigestUtils.md5DigestAsHex(
                    (requestText == null ? "" : requestText).getBytes(StandardCharsets.UTF_8)));
            log.setCreatedAt(LocalDateTime.now());
            mapper.insert(log);
        } catch (RuntimeException ignored) {
            // 调用日志失败不能影响主业务流程。
        }
    }

    private int estimateTokens(String text) {
        return text == null || text.isBlank() ? 0 : Math.max(1, text.length() / 2);
    }

    private String truncate(String value) {
        if (value == null) return null;
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }
}
