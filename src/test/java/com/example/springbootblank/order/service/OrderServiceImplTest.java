package com.example.springbootblank.order.service;

import com.example.springbootblank.auth.security.JwtService;
import com.example.springbootblank.auth.security.MerchantAuthGuard;
import com.example.springbootblank.cart.mapper.CartMapper;
import com.example.springbootblank.common.error.BusinessException;
import com.example.springbootblank.log.service.OpLogService;
import com.example.springbootblank.order.dto.OrderCreateRequest;
import com.example.springbootblank.order.entity.Order;
import com.example.springbootblank.order.mapper.OrderMapper;
import com.example.springbootblank.rider.mapper.RiderMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private CartMapper cartMapper;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private RiderMapper riderMapper;
    @Mock
    private MerchantAuthGuard merchantAuthGuard;
    @Mock
    private OpLogService opLogService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrderShouldInsertOrderItemsAndClearCart() {
        when(jwtService.parse("token")).thenReturn(new JwtService.JwtPrincipal(JwtService.TYPE_USER, 1001L, "u1", null));
        when(cartMapper.listSelectedCartForOrder(1001L)).thenReturn(List.of(
                Map.of("dishId", 10L, "dishName", "A", "unitPrice", new BigDecimal("12.50"), "quantity", 2),
                Map.of("dishId", 11L, "dishName", "B", "unitPrice", new BigDecimal("5.00"), "quantity", 1)
        ));
        doAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(2001L);
            return 1;
        }).when(orderMapper).insertOrder(any(Order.class));
        when(cartMapper.clearSelectedCart(1001L)).thenReturn(2);

        Map<String, Object> result = orderService.createOrder("Bearer token", new OrderCreateRequest(1L, "少辣"));

        assertEquals(2001L, result.get("orderId"));
        assertEquals(0, result.get("status"));
        assertEquals(new BigDecimal("30.00"), result.get("totalAmount"));
        verify(orderMapper).insertOrder(any(Order.class));
        verify(orderMapper, times(2)).insertOrderItem(any());
        verify(cartMapper).clearSelectedCart(1001L);
    }

    @Test
    void createOrderShouldThrowWhenCartEmpty() {
        when(jwtService.parse("token")).thenReturn(new JwtService.JwtPrincipal(JwtService.TYPE_USER, 1001L, "u1", null));
        when(cartMapper.listSelectedCartForOrder(1001L)).thenReturn(List.of());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder("Bearer token", new OrderCreateRequest(1L, null)));

        assertEquals(400, ex.getCode());
        verify(orderMapper, never()).insertOrder(any(Order.class));
        verify(cartMapper, never()).clearSelectedCart(anyLong());
    }

    @Test
    void createOrderShouldThrowWhenClearCartAffectedRowsIsZero() {
        when(jwtService.parse("token")).thenReturn(new JwtService.JwtPrincipal(JwtService.TYPE_USER, 1001L, "u1", null));
        when(cartMapper.listSelectedCartForOrder(1001L)).thenReturn(List.of(
                Map.of("dishId", 10L, "dishName", "A", "unitPrice", new BigDecimal("12.50"), "quantity", 1)
        ));
        doAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(2002L);
            return 1;
        }).when(orderMapper).insertOrder(any(Order.class));
        when(cartMapper.clearSelectedCart(1001L)).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder("Bearer token", new OrderCreateRequest(1L, null)));

        assertEquals(409, ex.getCode());
        verify(orderMapper).insertOrder(any(Order.class));
        verify(orderMapper).insertOrderItem(any());
        verify(cartMapper).clearSelectedCart(1001L);
    }

    @Test
    void acceptOrderShouldSucceedAndLog() {
        Order o = new Order();
        o.setId(500L);
        o.setShopId(1L);
        when(orderMapper.findOrderById(500L)).thenReturn(o);
        doNothing().when(merchantAuthGuard).requireShopAccess(any(), eq(1L));
        when(orderMapper.updateOrderStatusMerchant(500L, 1, 2)).thenReturn(1);

        orderService.acceptOrder("Bearer emp", 500L, null);

        verify(orderMapper).updateOrderStatusMerchant(500L, 1, 2);
        verify(opLogService).log(any(), any(), any(), any(), any());
    }

    @Test
    void acceptOrderShouldThrowWhenShopMismatch() {
        Order o = new Order();
        o.setId(500L);
        o.setShopId(99L);
        when(orderMapper.findOrderById(500L)).thenReturn(o);
        doThrow(new BusinessException(403, "无权访问该店铺数据"))
                .when(merchantAuthGuard).requireShopAccess(any(), eq(99L));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.acceptOrder("Bearer emp", 500L, null));
        assertEquals(403, ex.getCode());
    }
}

