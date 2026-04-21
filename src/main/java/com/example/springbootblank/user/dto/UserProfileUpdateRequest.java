package com.example.springbootblank.user.dto;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UserProfileUpdateRequest(
        @Size(max = 50) String nickname,
        @Size(max = 255) String avatar,
        @Size(max = 50) String receiverName,
        @Size(max = 20) String shippingPhone,
        @Size(max = 500) String shippingAddress,
        BigDecimal shippingLat,
        BigDecimal shippingLng
) {}
