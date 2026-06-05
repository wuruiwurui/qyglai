package com.qyglai.automation.dto;

/**
 * Python AI服务返回的文件解析并抽取结果。
 *
 * @param parsed 文件解析结果
 * @param extraction AI抽取结果
 */
public record ParseAndExtractResponse(
        ParsedFileResult parsed,
        ExtractionResult extraction
) {
}
