package com.example.springbootblank.dish.controller;

import com.example.springbootblank.common.api.ApiResponse;
import com.example.springbootblank.dish.dto.DishCreateRequest;
import com.example.springbootblank.dish.dto.DishStatusUpdateRequest;
import com.example.springbootblank.dish.dto.DishUpdateRequest;
import com.example.springbootblank.dish.service.DishService;
import com.example.springbootblank.shop.service.ShopService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api")
public class DishController {

    private final DishService dishService;
    private final ShopService shopService;

    public DishController(DishService dishService, ShopService shopService) {
        this.dishService = dishService;
        this.shopService = shopService;
    }

    @GetMapping("/merchant/dishes")
    public ApiResponse<Map<String, Object>> merchantDishes(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam Long shopId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String dishName,
            @RequestParam(required = false) Integer status
    ) {
        return ApiResponse.ok(dishService.merchantDishes(authorization, page, pageSize, shopId, categoryId, dishName, status));
    }

    @PostMapping("/merchant/dishes")
    public ApiResponse<Map<String, Object>> createDish(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody DishCreateRequest req
    ) {
        return ApiResponse.ok(dishService.createDish(authorization, req));
    }

    @PutMapping("/merchant/dishes/{id}")
    public ApiResponse<Void> updateDish(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @RequestBody DishUpdateRequest req
    ) {
        dishService.updateDish(authorization, id, req);
        return ApiResponse.ok();
    }

    @DeleteMapping("/merchant/dishes/{id}")
    public ApiResponse<Void> deleteDish(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id
    ) {
        dishService.deleteDish(authorization, id);
        return ApiResponse.ok();
    }

    @PatchMapping("/merchant/dishes/{id}/status")
    public ApiResponse<Void> updateDishStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @RequestBody DishStatusUpdateRequest req
    ) {
        dishService.updateDishStatus(authorization, id, req);
        return ApiResponse.ok();
    }

    @GetMapping("/user/shops")
    public ApiResponse<List<Map<String, Object>>> listShops() {
        return ApiResponse.ok(shopService.listUserShopOverviews());
    }

    @GetMapping("/user/dishes")
    public ApiResponse<List<Map<String, Object>>> userDishes(
            @RequestParam Long shopId,
            @RequestParam(required = false) Long categoryId
    ) {
        return ApiResponse.ok(dishService.userDishes(shopId, categoryId));
    }
}
