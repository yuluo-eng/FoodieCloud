package com.example.springbootblank.payment.controller;

import com.example.springbootblank.common.api.ApiResponse;
import com.example.springbootblank.payment.dto.PaymentCreateRequest;
import com.example.springbootblank.payment.dto.PaymentMockSuccessRequest;
import com.example.springbootblank.payment.service.PaymentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create")
    public ApiResponse<Map<String, Object>> create(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody PaymentCreateRequest req
    ) {
        return ApiResponse.ok(paymentService.create(authorization, req));
    }

    @PostMapping("/mock-success")
    public ApiResponse<Void> mockSuccess(@RequestBody PaymentMockSuccessRequest req) {
        paymentService.mockSuccess(req);
        return ApiResponse.ok();
    }

    @GetMapping("/{paymentNo}/status")
    public ApiResponse<Map<String, Object>> status(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable String paymentNo
    ) {
        return ApiResponse.ok(paymentService.status(authorization, paymentNo));
    }
}
