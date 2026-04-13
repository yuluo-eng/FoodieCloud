package com.example.springbootblank.category.dto;

public record CategoryCreateRequest(Long shopId, String categoryName, Integer sort, Integer status) {}
