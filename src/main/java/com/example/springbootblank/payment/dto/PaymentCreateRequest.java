package com.example.springbootblank.payment.dto;

public record PaymentCreateRequest(Long orderId, String payChannel) {}
