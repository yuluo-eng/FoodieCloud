package com.example.springbootblank.admin.dto;

public record AdminEmployeeBootstrapRequest(
        String username,
        String password,
        String realName,
        String phone,
        Long roleId
) {}
