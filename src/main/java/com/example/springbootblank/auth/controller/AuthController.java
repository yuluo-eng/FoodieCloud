package com.example.springbootblank.auth.controller;

import com.example.springbootblank.auth.dto.AuthMeResponse;
import com.example.springbootblank.auth.dto.LoginRequest;
import com.example.springbootblank.auth.dto.UserRegisterRequest;
import com.example.springbootblank.auth.service.AuthService;
import com.example.springbootblank.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/user/register")
    public ApiResponse<Map<String, Object>> userRegister(@Valid @RequestBody UserRegisterRequest req) {
        return ApiResponse.ok("注册成功", authService.register(req));
    }

    @PostMapping("/user/login")
    public ApiResponse<Map<String, Object>> userLogin(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.userLogin(req));
    }

    @PostMapping("/employee/login")
    public ApiResponse<Map<String, Object>> employeeLogin(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.employeeLogin(req));
    }

    @GetMapping("/me")
    public ApiResponse<AuthMeResponse> me(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return ApiResponse.ok(authService.me(authorization));
    }
}
