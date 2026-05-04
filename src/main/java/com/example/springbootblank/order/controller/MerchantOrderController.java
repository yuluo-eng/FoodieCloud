package com.example.springbootblank.order.controller;

import com.example.springbootblank.common.api.ApiResponse;
import com.example.springbootblank.order.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/merchant/orders")
public class MerchantOrderController {

    private final OrderService orderService;

    public MerchantOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam Long shopId,
            @RequestParam(required = false) Integer status
    ) {
        return ApiResponse.ok(orderService.merchantOrders(authorization, page, pageSize, shopId, status));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<Map<String, Object>> detail(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam Long shopId,
            @PathVariable Long orderId
    ) {
        return ApiResponse.ok(orderService.merchantOrderDetail(authorization, shopId, orderId));
    }

    @PatchMapping("/{orderId}/accept")
    public ApiResponse<Void> accept(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "SELF") String deliveryMode
    ) {
        orderService.acceptOrder(authorization, orderId, deliveryMode);
        return ApiResponse.ok();
    }

    @PatchMapping("/{orderId}/delivery")
    public ApiResponse<Void> delivery(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long orderId
    ) {
        orderService.deliveryOrder(authorization, orderId);
        return ApiResponse.ok();
    }

    @PatchMapping("/{orderId}/finish")
    public ApiResponse<Void> finish(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long orderId
    ) {
        orderService.finishOrder(authorization, orderId);
        return ApiResponse.ok();
    }
}
