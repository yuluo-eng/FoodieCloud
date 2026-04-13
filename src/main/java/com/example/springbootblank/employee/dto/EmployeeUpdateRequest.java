package com.example.springbootblank.employee.dto;

public record EmployeeUpdateRequest(
        String realName,
        String phone,
        Long shopId,
        Long roleId,
        Integer enabled
) {}

