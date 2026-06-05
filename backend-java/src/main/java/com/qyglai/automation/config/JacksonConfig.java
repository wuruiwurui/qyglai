package com.qyglai.automation.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JSON 序列化配置。
 *
 * <p>系统主键使用雪花 ID，数值长度超过 JavaScript 安全整数范围。
 * 因此 Long 类型统一按字符串输出，避免前端请求详情时出现 ID 精度丢失。</p>
 */
@Configuration
public class JacksonConfig {

    /**
     * 将 Long 和 long 序列化为字符串。
     *
     * @return Jackson 构建器定制器
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> builder
                .serializerByType(Long.class, ToStringSerializer.instance)
                .serializerByType(Long.TYPE, ToStringSerializer.instance);
    }
}
