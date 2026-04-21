package com.example.springbootblank.user.dto;

import java.math.BigDecimal;

public record UserProfileResponse(
        Long id,
        String username,
        String nickname,
        String avatar,
        String phone,
        String receiverName,
        String shippingPhone,
        String shippingAddress,
        BigDecimal shippingLat,
        BigDecimal shippingLng
) {}
