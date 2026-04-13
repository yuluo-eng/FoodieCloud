package com.example.springbootblank.category.controller;

import com.example.springbootblank.category.service.CategoryService;
import com.example.springbootblank.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/categories")
public class UserCategoryController {

    private final CategoryService categoryService;

    public UserCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list(@RequestParam Long shopId) {
        return ApiResponse.ok(categoryService.userList(shopId));
    }
}
