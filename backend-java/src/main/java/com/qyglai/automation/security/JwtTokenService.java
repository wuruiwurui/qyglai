package com.qyglai.automation.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qyglai.automation.config.SecurityProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 令牌服务。
 *
 * 使用标准 HMAC-SHA256 实现，避免额外依赖；生产环境只需替换密钥和过期策略。
 */
@Component
public class JwtTokenService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final SecurityProperties properties;

    public JwtTokenService(SecurityProperties properties) {
        this.properties = properties;
    }

    /**
     * 生成访问令牌。
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param roles 角色列表
     * @param permissions 权限列表
     * @return JWT字符串
     */
    public String createToken(Long userId, String username, List<String> roles, List<String> permissions) {
        long now = Instant.now().getEpochSecond();
        long exp = now + properties.getTokenTtlSeconds();
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = """
                {"sub":"%s","uid":%d,"roles":%s,"permissions":%s,"iat":%d,"exp":%d}
                """.formatted(escape(username), userId, toJson(roles), toJson(permissions), now, exp).trim();
        String unsigned = base64Url(header.getBytes(StandardCharsets.UTF_8)) + "." + base64Url(payload.getBytes(StandardCharsets.UTF_8));
        return unsigned + "." + sign(unsigned);
    }

    /**
     * 解析并验证令牌。
     *
     * @param token JWT字符串
     * @return 令牌载荷
     */
    public JwtPrincipal parse(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3 || !MessageDigest.isEqual(sign(parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
                throw new IllegalArgumentException("invalid token signature");
            }
            JsonNode payload = OBJECT_MAPPER.readTree(Base64.getUrlDecoder().decode(parts[1]));
            if (payload.path("exp").asLong() < Instant.now().getEpochSecond()) {
                throw new IllegalArgumentException("token expired");
            }
            List<String> roles = OBJECT_MAPPER.convertValue(payload.path("roles"), OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, String.class));
            List<String> permissions = OBJECT_MAPPER.convertValue(payload.path("permissions"), OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, String.class));
            return new JwtPrincipal(payload.path("uid").asLong(), payload.path("sub").asText(), roles, permissions);
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid token", ex);
        }
    }

    public long ttlSeconds() {
        return properties.getTokenTtlSeconds();
    }

    private String sign(String unsigned) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.getJwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return base64Url(mac.doFinal(unsigned.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("cannot sign jwt", ex);
        }
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String toJson(List<String> values) {
        try {
            return OBJECT_MAPPER.writeValueAsString(values);
        } catch (Exception ex) {
            throw new IllegalStateException("cannot serialize jwt claim", ex);
        }
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
