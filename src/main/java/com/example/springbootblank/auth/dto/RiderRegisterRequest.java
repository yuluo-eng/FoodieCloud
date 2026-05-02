package com.example.springbootblank.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RiderRegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 2, max = 50, message = "用户名长度 2～50")
        String username,
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 50, message = "密码长度 6～50")
        String password,
        @NotBlank(message = "真实姓名不能为空")
        @Size(max = 50, message = "真实姓名过长")
        String realName,
        @NotBlank(message = "手机号不能为空")
        @Size(max = 20, message = "手机号过长")
        String phone
) {
}
