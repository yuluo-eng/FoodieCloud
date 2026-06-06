package com.example.springbootblank.cart.dto;

/** PATCH /api/user/cart/{dishId}/selected 请求体：勾选状态，0 或 1。 */
public record CartSelectedUpdateRequest(Integer selected) {}
