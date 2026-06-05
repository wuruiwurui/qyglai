package com.qyglai.automation.dto;

import java.math.BigDecimal;

/**
 * 通用创建请求，用于当前工程骨架阶段的轻量数据录入。
 *
 * @param code 编码
 * @param name 名称
 * @param type 类型
 * @param content 内容
 * @param amount 金额
 */
public record SimpleCreateRequest(
        String code,
        String name,
        String type,
        String content,
        BigDecimal amount
) {
}

