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
@RequestMapping("/api/rider/orders")
public class RiderOrderController {

    private final OrderService orderService;

    public RiderOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/dispatch")
    public ApiResponse<Map<String, Object>> dispatch(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return ApiResponse.ok(orderService.riderDispatchOrders(authorization, page, pageSize));
    }

    @GetMapping("/current")
    public ApiResponse<Map<String, Object>> current(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return ApiResponse.ok(orderService.riderCurrentOrders(authorization));
    }

    @PatchMapping("/{orderId}/accept")
    public ApiResponse<Void> accept(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long orderId
    ) {
        orderService.riderAcceptOrder(authorization, orderId);
        return ApiResponse.ok();
    }

    @PatchMapping("/{orderId}/arrive-shop")
    public ApiResponse<Void> arriveShop(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long orderId
    ) {
        orderService.riderArriveShop(authorization, orderId);
        return ApiResponse.ok();
    }

    @PatchMapping("/{orderId}/pickup")
    public ApiResponse<Void> pickup(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long orderId
    ) {
        orderService.riderPickup(authorization, orderId);
        return ApiResponse.ok();
    }

    @PatchMapping("/{orderId}/delivered")
    public ApiResponse<Void> delivered(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long orderId
    ) {
        orderService.riderDelivered(authorization, orderId);
        return ApiResponse.ok();
    }
}
