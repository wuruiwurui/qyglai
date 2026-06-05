package com.qyglai.automation.dto;

import java.util.List;

/**
 * Python AI服务返回的文件解析结果。
 *
 * @param filename 文件名
 * @param contentType 内容类型
 * @param extension 扩展名
 * @param rawText 原始文本
 * @param charCount 字符数
 * @param warnings 解析提示
 */
public record ParsedFileResult(
        String filename,
        String contentType,
        String extension,
        String rawText,
        int charCount,
        List<String> warnings
) {
}
