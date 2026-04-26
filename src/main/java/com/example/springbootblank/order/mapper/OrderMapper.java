package com.example.springbootblank.order.mapper;

import com.example.springbootblank.order.entity.Order;
import com.example.springbootblank.order.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    int insertOrder(Order order);

    int insertOrderItem(OrderItem orderItem);

    long countUserOrders(@Param("userId") Long userId, @Param("status") Integer status);

    List<Order> listUserOrders(@Param("userId") Long userId,
                               @Param("status") Integer status,
                               @Param("offset") int offset,
                               @Param("pageSize") int pageSize);

    Order findUserOrderById(@Param("userId") Long userId, @Param("orderId") Long orderId);

    List<OrderItem> listOrderItems(@Param("orderId") Long orderId);

    int updateOrderStatus(@Param("id") Long id, @Param("fromStatus") Integer fromStatus, @Param("toStatus") Integer toStatus);

    int updateOrderCancelByUser(@Param("id") Long id);

    long countMerchantOrders(@Param("shopId") Long shopId, @Param("status") Integer status);

    List<Order> listMerchantOrders(@Param("shopId") Long shopId,
                                   @Param("status") Integer status,
                                   @Param("offset") int offset,
                                   @Param("pageSize") int pageSize);

    Order findOrderById(@Param("orderId") Long orderId);

    int updateOrderPaySuccess(@Param("id") Long id);

    Map<String, Object> findOrderPayInfo(@Param("orderId") Long orderId);

    long countMerchantOrdersPaidToday(@Param("shopId") Long shopId,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    BigDecimal sumMerchantRevenuePaidToday(@Param("shopId") Long shopId,
                                             @Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);

    long countDispatchOrders();

    List<Order> listDispatchOrders(@Param("offset") int offset, @Param("pageSize") int pageSize);

    int riderAcceptOrder(@Param("orderId") Long orderId, @Param("riderId") Long riderId);

    List<Order> listRiderCurrentOrders(@Param("riderId") Long riderId);

    int riderArriveShop(@Param("orderId") Long orderId, @Param("riderId") Long riderId);

    int riderPickup(@Param("orderId") Long orderId, @Param("riderId") Long riderId);

    int riderDelivered(@Param("orderId") Long orderId, @Param("riderId") Long riderId);
}
