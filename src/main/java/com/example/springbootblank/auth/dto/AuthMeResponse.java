package com.example.springbootblank.auth.dto;

/**
 * @param displayName 展示名：顾客为昵称（无则用户名），员工为真实姓名（无则用户名）
 * @param avatar 顾客头像 URL，员工为 null
 */
public record AuthMeResponse(Long id, String type, String username, String roleCode, String displayName, String avatar, Long shopId) {}

