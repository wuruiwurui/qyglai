package com.qyglai.automation.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.qyglai.automation.dto.AiRuntimeConfig;
import com.qyglai.automation.dto.ExtractionResult;
import org.springframework.stereotype.Service;

/**
 * Java 文档结构化抽取服务，真实模型与确定性规则协同工作。
 */
@Service
public class JavaDocumentExtractionService {

    private static final Pattern YUAN_AMOUNT = Pattern.compile("(?:¥|￥|金额|价税合计|小写|合计)[^0-9]{0,30}([0-9,]+(?:\\.[0-9]{1,2})?)");
    private static final Pattern INVOICE_NO = Pattern.compile("(?:发票号码|发票号|号码)[:：\\s]*([0-9]{8,30})");
    private final JavaAiModelGateway modelGateway;
    private final JavaAiModelConfigService configService;

    public JavaDocumentExtractionService(JavaAiModelGateway modelGateway, JavaAiModelConfigService configService) {
        this.modelGateway = modelGateway;
        this.configService = configService;
    }

    public ExtractionResult extract(String rawText, String requestedScenario) {
        String scenario = detectScenario(rawText, requestedScenario);
        ExtractionResult rules = ruleExtraction(rawText, scenario);
        AiRuntimeConfig config = configService.getConfig();
        if ("rules_only".equals(config.fileExtractionMode())) return rules;
        JsonNode node = modelGateway.generateJson(
                "你是企业发票和合同结构化抽取引擎。必须忠于原文，只输出合法JSON，不输出Markdown。金额不得使用发票号、订单号、航班号。",
                "场景：" + scenario + "\n原文：\n" + limit(rawText, 24000)
                        + "\n只输出格式：{\"scenario\":\"invoice或contract或general\",\"confidence\":0.95,"
                        + "\"fields\":{\"amount\":\"数字\"},\"risks\":[],\"reviewRequired\":false}。"
                        + "发票字段使用invoice_no、invoice_code、invoice_date、buyer_name、buyer_tax_no、seller_name、seller_tax_no、"
                        + "line_amount、tax_amount、total_amount、tax_rate、amount；合同字段使用contract_no、party_a、party_b、amount、payment_terms、risk_level。"
        );
        if (node == null || !node.path("fields").isObject()) return rules;
        Map<String, String> modelFields = new LinkedHashMap<>();
        node.path("fields").fields().forEachRemaining(entry -> modelFields.put(entry.getKey(), entry.getValue().asText("")));
        Map<String, String> merged = new LinkedHashMap<>();
        if ("model_first".equals(config.fileExtractionMode())) {
            merged.putAll(rules.fields());
            merged.putAll(modelFields);
        } else {
            merged.putAll(modelFields);
            merged.putAll(rules.fields());
        }
        if (blank(merged.get("amount"))) merged.put("amount", first(merged.get("total_amount"), rules.fields().get("amount"), "0"));
        List<String> risks = new ArrayList<>();
        node.path("risks").forEach(item -> risks.add(item.asText()));
        rules.risks().forEach(risk -> { if (!risks.contains(risk)) risks.add(risk); });
        return new ExtractionResult(node.path("scenario").asText(scenario),
                Math.max(rules.confidence(), node.path("confidence").asDouble(rules.confidence())),
                merged, risks, node.path("reviewRequired").asBoolean(false) || rules.reviewRequired());
    }

    private ExtractionResult ruleExtraction(String text, String scenario) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("scenario", scenario);
        BigDecimal amount = amount(text);
        fields.put("amount", amount.toPlainString());
        List<String> risks = new ArrayList<>();
        if (amount.compareTo(new BigDecimal("100000")) >= 0) risks.add("金额超过10万元，建议人工复核");
        if ("invoice".equals(scenario)) {
            fields.put("total_amount", amount.toPlainString());
            Matcher invoiceNo = INVOICE_NO.matcher(text);
            if (invoiceNo.find()) fields.put("invoice_no", invoiceNo.group(1));
        }
        if ("contract".equals(scenario) && Pattern.compile("违约|赔偿|逾期").matcher(text).find()) {
            risks.add("存在违约、赔偿或逾期条款，需要法务复核");
        }
        return new ExtractionResult(scenario, text.isBlank() ? 0.2 : 0.82, fields, risks, !risks.isEmpty() || text.isBlank());
    }

    private BigDecimal amount(String text) {
        Matcher matcher = YUAN_AMOUNT.matcher(text == null ? "" : text);
        BigDecimal selected = BigDecimal.ZERO;
        while (matcher.find()) {
            try {
                BigDecimal candidate = new BigDecimal(matcher.group(1).replace(",", ""));
                if (candidate.signum() > 0 && candidate.compareTo(new BigDecimal("1000000000")) < 0) selected = selected.max(candidate);
            } catch (NumberFormatException ignored) {
                // 忽略无效候选金额。
            }
        }
        return selected;
    }

    private String detectScenario(String text, String requested) {
        String normalized = requested == null ? "general" : requested.toLowerCase();
        if (text != null && Pattern.compile("发票|价税合计|税率|税额").matcher(text).find()) return "invoice";
        if ("contract".equals(normalized) || text != null && Pattern.compile("合同|甲方|乙方|违约").matcher(text).find()) return "contract";
        return normalized;
    }

    private String limit(String text, int length) {
        if (text == null) return "";
        return text.length() <= length ? text : text.substring(0, length);
    }

    private boolean blank(String value) {
        return value == null || value.isBlank() || "None".equalsIgnoreCase(value);
    }

    private String first(String... values) {
        for (String value : values) if (!blank(value)) return value;
        return "";
    }
}
