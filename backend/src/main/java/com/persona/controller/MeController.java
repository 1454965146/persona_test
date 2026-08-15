package com.persona.controller;

import com.persona.dto.ApiResponse;
import com.persona.model.User;
import com.persona.service.AuthService;
import com.persona.service.HistoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeController {
    private final AuthService authService;
    private final HistoryService historyService;

    public MeController(AuthService authService, HistoryService historyService) {
        this.authService = authService;
        this.historyService = historyService;
    }

    @GetMapping
    public ApiResponse<?> me() {
        return ApiResponse.success(authService.currentUser());
    }

    @GetMapping("/history")
    public ApiResponse<?> history() {
        User user = authService.requireCurrentUser();
        return ApiResponse.success(historyService.getHistory(user));
    }
}
