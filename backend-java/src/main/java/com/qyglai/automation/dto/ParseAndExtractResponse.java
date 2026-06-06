package com.qyglai.automation.dto;

/**
 * Java AI 文件解析与字段抽取结果。
 *
 * @param parsed 文件解析结果
 * @param extraction AI 字段抽取结果
 */
public record ParseAndExtractResponse(
        ParsedFileResult parsed,
        ExtractionResult extraction
) {
}
