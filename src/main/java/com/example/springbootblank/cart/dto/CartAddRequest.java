package com.example.springbootblank.cart.dto;

/** POST /api/user/cart 请求体：指定菜品及加购数量。 */
public record CartAddRequest(Long dishId, Integer quantity) {}
