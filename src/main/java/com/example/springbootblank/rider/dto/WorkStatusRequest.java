package com.example.springbootblank.rider.dto;

import jakarta.validation.constraints.NotBlank;

public record WorkStatusRequest(
        @NotBlank(message = "workStatus 不能为空")
        String workStatus
) {
}
