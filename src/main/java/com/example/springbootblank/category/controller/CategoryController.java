package com.example.springbootblank.category.controller;

import com.example.springbootblank.category.dto.CategoryCreateRequest;
import com.example.springbootblank.category.dto.CategoryUpdateRequest;
import com.example.springbootblank.category.service.CategoryService;
import com.example.springbootblank.common.api.ApiResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Long shopId
    ) {
        return ApiResponse.ok(categoryService.list(authorization, shopId));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody CategoryCreateRequest req
    ) {
        return ApiResponse.ok(categoryService.create(authorization, req));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @RequestBody CategoryUpdateRequest req
    ) {
        categoryService.update(authorization, id, req);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id
    ) {
        categoryService.delete(authorization, id);
        return ApiResponse.ok();
    }
}
