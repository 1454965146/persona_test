package com.persona.controller;

import com.persona.dto.ApiResponse;
import com.persona.dto.PaymentCreateRequest;
import com.persona.model.User;
import com.persona.service.AuthService;
import com.persona.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    private final PaymentService paymentService;
    private final AuthService authService;

    public PaymentController(PaymentService paymentService, AuthService authService) {
        this.paymentService = paymentService;
        this.authService = authService;
    }

    @PostMapping("/order")
    public ApiResponse<?> createOrder(@Valid @RequestBody PaymentCreateRequest request) {
        User user = authService.requireCurrentUser();
        return ApiResponse.success(paymentService.createOrder(request.getReportCode(), user));
    }

    @GetMapping("/order/{orderNo}")
    public ApiResponse<?> getOrder(@PathVariable String orderNo) {
        User user = authService.requireCurrentUser();
        return ApiResponse.success(paymentService.getOrder(orderNo, user));
    }

    @PostMapping("/{orderNo}/mock-success")
    public ApiResponse<?> mockPaySuccess(@PathVariable String orderNo) {
        User user = authService.requireCurrentUser();
        return ApiResponse.success(paymentService.mockPaySuccess(orderNo, user));
    }
}
