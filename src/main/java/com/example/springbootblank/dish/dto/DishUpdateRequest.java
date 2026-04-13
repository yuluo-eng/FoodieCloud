package com.example.springbootblank.dish.dto;

import java.math.BigDecimal;

public record DishUpdateRequest(
        Long shopId,
        Long categoryId,
        String dishName,
        BigDecimal price,
        String imageUrl,
        String description,
        Integer stock,
        Integer status
) {}

