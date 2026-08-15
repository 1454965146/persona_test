package com.persona.controller;

import com.persona.dto.ApiResponse;
import com.persona.dto.CompareGenerateRequest;
import com.persona.model.User;
import com.persona.service.AuthService;
import com.persona.service.CompareService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/compare")
public class CompareController {
    private final CompareService compareService;
    private final AuthService authService;

    public CompareController(CompareService compareService, AuthService authService) {
        this.compareService = compareService;
        this.authService = authService;
    }

    @PostMapping("/generate")
    public ApiResponse<?> generateComparison(@Valid @RequestBody CompareGenerateRequest request) {
        User user = authService.requireCurrentUser();
        return ApiResponse.success(compareService.generateComparison(
                request.getReportCodeA(), request.getReportCodeB(), request.getRelationshipType(), user));
    }

    @GetMapping("/{comparisonId}")
    public ApiResponse<?> getComparison(@PathVariable Long comparisonId) {
        User user = authService.requireCurrentUser();
        return ApiResponse.success(compareService.getComparison(comparisonId, user));
    }

    @PostMapping("/{comparisonId}/retry")
    public ApiResponse<?> retryComparison(@PathVariable Long comparisonId) {
        User user = authService.requireCurrentUser();
        compareService.retryComparison(comparisonId, user);
        return ApiResponse.success(true);
    }
}
