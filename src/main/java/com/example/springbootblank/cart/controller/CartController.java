package com.example.springbootblank.cart.controller;

import com.example.springbootblank.cart.dto.CartAddRequest;
import com.example.springbootblank.cart.dto.CartQuantityUpdateRequest;
import com.example.springbootblank.cart.dto.CartSelectedUpdateRequest;
import com.example.springbootblank.cart.service.CartService;
import com.example.springbootblank.common.api.ApiResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return ApiResponse.ok(cartService.listCart(authorization));
    }

    @PostMapping
    public ApiResponse<Void> add(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody CartAddRequest req
    ) {
        cartService.addToCart(authorization, req);
        return ApiResponse.ok();
    }

    @PutMapping("/{dishId}")
    public ApiResponse<Void> update(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long dishId,
            @RequestBody CartQuantityUpdateRequest req
    ) {
        cartService.updateQuantity(authorization, dishId, req);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{dishId}")
    public ApiResponse<Void> delete(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long dishId
    ) {
        cartService.deleteItem(authorization, dishId);
        return ApiResponse.ok();
    }

    @PatchMapping("/{dishId}/selected")
    public ApiResponse<Void> updateSelected(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long dishId,
            @RequestBody CartSelectedUpdateRequest req
    ) {
        cartService.updateSelected(authorization, dishId, req);
        return ApiResponse.ok();
    }

    @DeleteMapping("/clear")
    public ApiResponse<Void> clear(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        cartService.clear(authorization);
        return ApiResponse.ok();
    }
}
