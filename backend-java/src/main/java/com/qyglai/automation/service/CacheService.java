package com.qyglai.automation.service;

import java.time.Duration;
import java.util.Optional;

import com.qyglai.automation.config.AutomationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 缓存服务，统一封装 Redis 读写。
 */
@Service
public class CacheService {

    private final StringRedisTemplate redisTemplate;
    private final AutomationProperties properties;

    public CacheService(StringRedisTemplate redisTemplate, AutomationProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    /**
     * 写入字符串缓存。
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param ttl 过期时间
     */
    public void put(String key, String value, Duration ttl) {
        if (properties.getCache().isRedisEnabled()) {
            redisTemplate.opsForValue().set(key, value, ttl);
        }
    }

    /**
     * 读取字符串缓存。
     *
     * @param key 缓存键
     * @return 缓存值
     */
    public Optional<String> get(String key) {
        if (!properties.getCache().isRedisEnabled()) {
            return Optional.empty();
        }
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }
}

