package com.qyglai.automation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 安全配置属性，集中管理 JWT 密钥和过期时间。
 */
@ConfigurationProperties(prefix = "automation.security")
public class SecurityProperties {

    /**
     * JWT 签名密钥，生产环境必须通过环境变量覆盖。
     */
    private String jwtSecret = "qyglai-local-secret-change-in-production-2026";

    /**
     * Token 有效期，单位秒。
     */
    private long tokenTtlSeconds = 86400;

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public long getTokenTtlSeconds() {
        return tokenTtlSeconds;
    }

    public void setTokenTtlSeconds(long tokenTtlSeconds) {
        this.tokenTtlSeconds = tokenTtlSeconds;
    }
}
