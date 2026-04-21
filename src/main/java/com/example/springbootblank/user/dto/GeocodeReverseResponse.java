package com.example.springbootblank.user.dto;

import java.math.BigDecimal;

/**
 * 逆地理编码结果（供填写收货地址）。
 */
public record GeocodeReverseResponse(
        String displayName,
        BigDecimal latitude,
        BigDecimal longitude
) {}
