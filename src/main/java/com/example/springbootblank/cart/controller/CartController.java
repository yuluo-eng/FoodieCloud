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

/**
 * 用户端购物车 REST 接口。
 * <p>
 * 路径前缀 {@code /api/user/cart}，仅顾客（USER Token）可操作。
 * 负责接收 HTTP 请求并委托 {@link com.example.springbootblank.cart.service.CartService} 处理，
 * 不包含业务规则（校验、合并数量等均在 Service 层）。
 */
@RestController
@RequestMapping("/api/user/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /** 查询当前登录用户的购物车列表（含菜品名、单价、数量、是否勾选）。 */
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return ApiResponse.ok(cartService.listCart(authorization));
    }

    /** 将菜品加入购物车；若已存在则累加数量。 */
    @PostMapping
    public ApiResponse<Void> add(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody CartAddRequest req
    ) {
        cartService.addToCart(authorization, req);
        return ApiResponse.ok();
    }

    /** 修改购物车中某菜品的数量。 */
    @PutMapping("/{dishId}")
    public ApiResponse<Void> update(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long dishId,
            @RequestBody CartQuantityUpdateRequest req
    ) {
        cartService.updateQuantity(authorization, dishId, req);
        return ApiResponse.ok();
    }

    /** 从购物车移除某一菜品。 */
    @DeleteMapping("/{dishId}")
    public ApiResponse<Void> delete(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long dishId
    ) {
        cartService.deleteItem(authorization, dishId);
        return ApiResponse.ok();
    }

    /** 勾选 / 取消勾选某菜品（下单时只结算 selected=1 的项）。 */
    @PatchMapping("/{dishId}/selected")
    public ApiResponse<Void> updateSelected(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long dishId,
            @RequestBody CartSelectedUpdateRequest req
    ) {
        cartService.updateSelected(authorization, dishId, req);
        return ApiResponse.ok();
    }

    /** 清空当前用户购物车（全部条目）。 */
    @DeleteMapping("/clear")
    public ApiResponse<Void> clear(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        cartService.clear(authorization);
        return ApiResponse.ok();
    }
}
