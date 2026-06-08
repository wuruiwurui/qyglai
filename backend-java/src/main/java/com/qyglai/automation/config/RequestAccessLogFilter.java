package com.qyglai.automation.config;

import com.qyglai.automation.security.JwtPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 记录接口访问日志，便于在 IDEA 控制台定位请求问题。
 */
@Component
public class RequestAccessLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestAccessLogFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String TRACE_ID_KEY = "traceId";

    /**
     * 仅记录后端接口，避免前端静态资源和接口文档产生大量无效日志。
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !uri.startsWith("/api/") && !uri.startsWith("/actuator/");
    }

    /**
     * 输出请求方法、路径、状态码、耗时、当前用户和客户端地址。
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String traceId = resolveTraceId(request);
        MDC.put(TRACE_ID_KEY, traceId);
        response.setHeader(REQUEST_ID_HEADER, traceId);
        long startedAt = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startedAt;
            writeAccessLog(
                    response.getStatus(),
                    request.getMethod(),
                    request.getRequestURI(),
                    durationMs,
                    resolveCurrentUser(),
                    resolveClientIp(request));
            MDC.remove(TRACE_ID_KEY);
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId != null && !requestId.isBlank() && requestId.length() <= 64) {
            return requestId;
        }
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String resolveCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "anonymous";
        }
        if (authentication.getPrincipal() instanceof JwtPrincipal principal) {
            return principal.username();
        }
        return authentication.getName();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeAccessLog(
            int status,
            String method,
            String uri,
            long durationMs,
            String user,
            String clientIp) {
        String template = "HTTP访问 method={} uri={} status={} durationMs={} user={} ip={}";
        if (status >= 500) {
            log.error(template, method, uri, status, durationMs, user, clientIp);
        } else if (status >= 400) {
            log.warn(template, method, uri, status, durationMs, user, clientIp);
        } else {
            log.info(template, method, uri, status, durationMs, user, clientIp);
        }
    }
}
