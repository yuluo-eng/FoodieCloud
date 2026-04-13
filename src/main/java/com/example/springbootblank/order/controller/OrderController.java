package com.example.springbootblank.order.controller;

import com.example.springbootblank.common.api.ApiResponse;
import com.example.springbootblank.order.dto.OrderCreateRequest;
import com.example.springbootblank.order.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody OrderCreateRequest req
    ) {
        return ApiResponse.ok("下单成功", orderService.createOrder(authorization, req));
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status
    ) {
        return ApiResponse.ok(orderService.userOrders(authorization, page, pageSize, status));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<Map<String, Object>> detail(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long orderId
    ) {
        return ApiResponse.ok(orderService.userOrderDetail(authorization, orderId));
    }

    @PatchMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancel(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long orderId
    ) {
        orderService.cancelUserOrder(authorization, orderId);
        return ApiResponse.ok();
    }
}
