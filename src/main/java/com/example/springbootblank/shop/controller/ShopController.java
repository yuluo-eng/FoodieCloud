package com.example.springbootblank.shop.controller;

import com.example.springbootblank.common.api.ApiResponse;
import com.example.springbootblank.shop.dto.ShopBusinessStatusUpdateRequest;
import com.example.springbootblank.shop.dto.ShopUpdateRequest;
import com.example.springbootblank.shop.service.ShopService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/merchant/shop")
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping("/{shopId}")
    public ApiResponse<Map<String, Object>> getShop(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long shopId
    ) {
        return ApiResponse.ok(shopService.getShop(authorization, shopId));
    }

    @PutMapping("/{shopId}")
    public ApiResponse<Void> updateShop(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long shopId,
            @RequestBody ShopUpdateRequest req
    ) {
        shopService.updateShop(authorization, shopId, req);
        return ApiResponse.ok();
    }

    @PatchMapping("/{shopId}/business-status")
    public ApiResponse<Void> updateBusinessStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long shopId,
            @RequestBody ShopBusinessStatusUpdateRequest req
    ) {
        shopService.updateBusinessStatus(authorization, shopId, req);
        return ApiResponse.ok();
    }
}
