package com.persona.controller;

import com.persona.dto.ApiResponse;
import com.persona.dto.ReportGenerateRequest;
import com.persona.model.User;
import com.persona.service.AuthService;
import com.persona.service.ReportService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/report")
public class ReportController {
    private final ReportService reportService;
    private final AuthService authService;

    public ReportController(ReportService reportService, AuthService authService) {
        this.reportService = reportService;
        this.authService = authService;
    }

    @PostMapping("/generate")
    public ApiResponse<?> generateReport(@Valid @RequestBody ReportGenerateRequest request) {
        User user = authService.requireCurrentUser();
        return ApiResponse.success(reportService.generateReport(request.getSessionCode(), request.getNickname(), user));
    }

    @GetMapping("/{reportCode}")
    public ApiResponse<?> getReport(@PathVariable String reportCode) {
        User user = authService.requireCurrentUser();
        return ApiResponse.success(reportService.getReport(reportCode, user));
    }
}
