package com.qyglai.automation.controller;

import com.qyglai.automation.common.ApiResponse;
import com.qyglai.automation.dto.LoginRequest;
import com.qyglai.automation.dto.LoginResponse;
import com.qyglai.automation.security.JwtPrincipal;
import com.qyglai.automation.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录认证接口。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录。
     *
     * @param request 登录请求
     * @return 登录信息
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    /**
     * 查询当前登录用户。
     *
     * @param authentication 当前认证信息
     * @return 当前用户
     */
    @GetMapping("/me")
    public ApiResponse<LoginResponse> me(Authentication authentication) {
        return ApiResponse.ok(authService.me((JwtPrincipal) authentication.getPrincipal()));
    }
}
