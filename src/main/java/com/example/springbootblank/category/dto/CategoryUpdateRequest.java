package com.example.springbootblank.category.dto;

public record CategoryUpdateRequest(Long shopId, String categoryName, Integer sort, Integer status) {}
