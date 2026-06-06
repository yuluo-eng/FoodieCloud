package com.example.springbootblank.cart.dto;

/** PUT /api/user/cart/{dishId} 请求体：将某菜品数量改为指定值。 */
public record CartQuantityUpdateRequest(Integer quantity) {}
