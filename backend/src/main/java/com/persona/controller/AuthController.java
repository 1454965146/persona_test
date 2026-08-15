package com.persona.controller;

import com.persona.dto.ApiResponse;
import com.persona.dto.DevLoginRequest;
import com.persona.dto.DevRegisterRequest;
import com.persona.dto.WechatLoginRequest;
import com.persona.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/wechat/login")
    public ApiResponse<?> wechatLogin(@RequestBody WechatLoginRequest request) {
        return ApiResponse.success(authService.wechatLogin(request.getCode(), request.getNickname()));
    }

    @PostMapping("/dev/login")
    public ApiResponse<?> devLogin(@RequestBody DevLoginRequest request) {
        return ApiResponse.success(authService.devLogin(request.getUsername(), request.getPassword()));
    }

    @PostMapping("/dev/register")
    public ApiResponse<?> devRegister(@RequestBody DevRegisterRequest request) {
        return ApiResponse.success(authService.devRegister(
                request.getUsername(), request.getPassword(), request.getNickname()));
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = null;
        if (authorization != null && authorization.startsWith("Bearer ")) token = authorization.substring(7).trim();
        authService.logout(token);
        return ApiResponse.success(true);
    }
}
