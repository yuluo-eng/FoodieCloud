package com.example.springbootblank.admin.dto;

public record AdminShopCreateRequest(
        String shopName,
        String address,
        String phone,
        String notice,
        Integer businessStatus
) {}
