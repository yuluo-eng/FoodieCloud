package com.example.springbootblank.payment.service;

import com.example.springbootblank.payment.dto.PaymentCreateRequest;
import com.example.springbootblank.payment.dto.PaymentMockSuccessRequest;

import java.util.Map;

public interface PaymentService {

    Map<String, Object> create(String authorization, PaymentCreateRequest req);

    void mockSuccess(PaymentMockSuccessRequest req);

    Map<String, Object> status(String authorization, String paymentNo);
}
