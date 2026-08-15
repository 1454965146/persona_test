package com.persona.controller;

import com.persona.dto.ApiResponse;
import com.persona.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
public class PublicConfigController {
    private final AuthService authService;

    public PublicConfigController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/public")
    public ApiResponse<?> publicConfig() {
        return ApiResponse.success(authService.publicConfig());
    }
}
