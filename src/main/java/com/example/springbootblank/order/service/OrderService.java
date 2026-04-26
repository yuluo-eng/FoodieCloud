package com.example.springbootblank.order.service;

import com.example.springbootblank.order.dto.OrderCreateRequest;

import java.util.Map;

public interface OrderService {

    Map<String, Object> createOrder(String authorization, OrderCreateRequest req);

    Map<String, Object> userOrders(String authorization, int page, int pageSize, Integer status);

    Map<String, Object> userOrderDetail(String authorization, Long orderId);

    void cancelUserOrder(String authorization, Long orderId);

    Map<String, Object> merchantOrders(String authorization, int page, int pageSize, Long shopId, Integer status);

    Map<String, Object> merchantOrderDetail(String authorization, Long shopId, Long orderId);

    void acceptOrder(String authorization, Long orderId);

    void deliveryOrder(String authorization, Long orderId);

    void finishOrder(String authorization, Long orderId);

    Map<String, Object> riderDispatchOrders(String authorization, int page, int pageSize);

    Map<String, Object> riderCurrentOrders(String authorization);

    void riderAcceptOrder(String authorization, Long orderId);

    void riderArriveShop(String authorization, Long orderId);

    void riderPickup(String authorization, Long orderId);

    void riderDelivered(String authorization, Long orderId);
}
