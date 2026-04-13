package com.example.springbootblank.employee.dto;

public record EmployeeCreateRequest(
        String username,
        String password,
        String realName,
        String phone,
        Long shopId,
        Long roleId,
        Integer enabled
) {}

