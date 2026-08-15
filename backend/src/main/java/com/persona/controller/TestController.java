package com.persona.controller;

import com.persona.dto.AnswerSubmitRequest;
import com.persona.dto.ApiResponse;
import com.persona.model.User;
import com.persona.service.AuthService;
import com.persona.service.TestService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/test")
public class TestController {
    private final TestService testService;
    private final AuthService authService;

    public TestController(TestService testService, AuthService authService) {
        this.testService = testService;
        this.authService = authService;
    }

    @GetMapping("/questions")
    public ApiResponse<?> getQuestions() { return ApiResponse.success(testService.getQuestions()); }

    @PostMapping("/submit")
    public ApiResponse<?> submitAnswers(@Valid @RequestBody AnswerSubmitRequest request) {
        User user = authService.requireCurrentUser();
        return ApiResponse.success(testService.submitAnswers(request.getAnswers(), user));
    }
}
