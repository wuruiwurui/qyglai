package com.qyglai.automation.config;

import com.qyglai.automation.security.JwtAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasAuthority;
import static org.springframework.security.authorization.AuthorizationManagers.anyOf;

/**
 * Spring Security配置。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * 访问日志过滤器仅由 Spring Security 调用，确保日志能够读取认证后的用户信息。
     */
    @Bean
    public FilterRegistrationBean<RequestAccessLogFilter> disableAccessLogFilterAutoRegistration(
            RequestAccessLogFilter requestAccessLogFilter) {
        FilterRegistrationBean<RequestAccessLogFilter> registration = new FilterRegistrationBean<>(requestAccessLogFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RequestAccessLogFilter requestAccessLogFilter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/health", "/actuator/**", "/swagger-ui/**", "/v3/api-docs/**", "/doc.html", "/webjars/**").permitAll()
                        .requestMatchers("/api/files/**").access(anyOf(hasAuthority("*"), hasAuthority("file:view")))
                        .requestMatchers("/api/contracts/**").access(anyOf(hasAuthority("*"), hasAuthority("contract:view")))
                        .requestMatchers("/api/invoices/**", "/api/reconciliation/**").access(anyOf(hasAuthority("*"), hasAuthority("finance:view")))
                        .requestMatchers("/api/tickets/**").access(anyOf(hasAuthority("*"), hasAuthority("ticket:view")))
                        .requestMatchers("/api/sales/**").access(anyOf(hasAuthority("*"), hasAuthority("sales:view")))
                        .requestMatchers("/api/kb/**").access(anyOf(hasAuthority("*"), hasAuthority("kb:view")))
                        .requestMatchers("/api/reports/**", "/api/boss-assistant/**").access(anyOf(hasAuthority("*"), hasAuthority("report:view")))
                        .requestMatchers("/api/workflows/**").access(anyOf(hasAuthority("*"), hasAuthority("workflow:view")))
                        .requestMatchers("/api/review/**").access(anyOf(hasAuthority("*"), hasAuthority("review:view")))
                        .requestMatchers("/api/notifications/**").access(anyOf(hasAuthority("*"), hasAuthority("notification:view")))
                        .requestMatchers("/api/integrations/**").access(anyOf(hasAuthority("*"), hasAuthority("integration:view")))
                        .requestMatchers("/api/ai-governance/**").access(anyOf(hasAuthority("*"), hasAuthority("ai:manage")))
                        .requestMatchers("/api/ai-tasks/**").authenticated()
                        .requestMatchers("/api/audit/**").access(anyOf(hasAuthority("*"), hasAuthority("audit:view")))
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(requestAccessLogFilter, JwtAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
