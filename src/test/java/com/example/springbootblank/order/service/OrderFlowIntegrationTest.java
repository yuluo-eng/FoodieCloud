package com.example.springbootblank.order.service;

import com.example.springbootblank.auth.security.JwtService;
import com.example.springbootblank.auth.security.MerchantAuthGuard;
import com.example.springbootblank.cart.mapper.CartMapper;
import com.example.springbootblank.common.error.BusinessException;
import com.example.springbootblank.dish.mapper.DishMapper;
import com.example.springbootblank.log.service.OpLogService;
import com.example.springbootblank.order.dto.OrderCreateRequest;
import com.example.springbootblank.order.entity.Order;
import com.example.springbootblank.order.entity.OrderItem;
import com.example.springbootblank.order.mapper.OrderMapper;
import com.example.springbootblank.payment.dto.PaymentCreateRequest;
import com.example.springbootblank.payment.dto.PaymentMockSuccessRequest;
import com.example.springbootblank.payment.mapper.PaymentMapper;
import com.example.springbootblank.payment.service.PaymentServiceImpl;
import com.example.springbootblank.rider.entity.Rider;
import com.example.springbootblank.rider.mapper.RiderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 模拟 下单→支付→商家接单→商家配送→完成 全链路。
 * 由于无真实 DB，用 Mockito 桩代替 Mapper 调用。
 */
@ExtendWith(MockitoExtension.class)
class OrderFlowIntegrationTest {

    @Mock private JwtService jwtService;
    @Mock private CartMapper cartMapper;
    @Mock private OrderMapper orderMapper;
    @Mock private RiderMapper riderMapper;
    @Mock private MerchantAuthGuard merchantAuthGuard;
    @Mock private OpLogService opLogService;
    @Mock private PaymentMapper paymentMapper;
    @Mock private DishMapper dishMapper;

    private OrderServiceImpl orderService;
    private PaymentServiceImpl paymentService;

    private static final long USER_ID = 100L;
    private static final long ORDER_ID = 5001L;
    private static final long SHOP_ID = 1L;
    private static final String USER_AUTH = "Bearer user-token";
    private static final String MERCHANT_AUTH = "Bearer emp-token";

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(jwtService, cartMapper, orderMapper,
                riderMapper, merchantAuthGuard, opLogService);
        paymentService = new PaymentServiceImpl(jwtService, paymentMapper, orderMapper,
                dishMapper, opLogService);
    }

    @Test
    void fullOrderFlowWithoutRider() {
        // 1) User creates order
        when(jwtService.parse("user-token")).thenReturn(
                new JwtService.JwtPrincipal(JwtService.TYPE_USER, USER_ID, "u1", null));
        when(cartMapper.listSelectedCartForOrder(USER_ID)).thenReturn(List.of(
                Map.of("dishId", 10L, "dishName", "宫保鸡丁", "shopId", SHOP_ID, "unitPrice", new BigDecimal("25.00"), "quantity", 2)
        ));
        doAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(ORDER_ID);
            return 1;
        }).when(orderMapper).insertOrder(any(Order.class));
        when(cartMapper.clearSelectedCart(USER_ID)).thenReturn(1);

        Map<String, Object> createResult = orderService.createOrder(USER_AUTH, new OrderCreateRequest(SHOP_ID, "少辣"));
        assertEquals(ORDER_ID, createResult.get("orderId"));
        assertEquals(new BigDecimal("50.00"), createResult.get("totalAmount"));

        // 2) Payment success (mock the whole payment flow in one step)
        when(paymentMapper.findPaymentByNo("PAY001")).thenReturn(Map.of(
                "orderId", ORDER_ID, "payStatus", 0));
        when(paymentMapper.markPaymentSuccess("PAY001")).thenReturn(1);
        when(orderMapper.updateOrderPaySuccess(ORDER_ID)).thenReturn(1);
        OrderItem item = new OrderItem();
        item.setDishId(10L);
        item.setDishName("宫保鸡丁");
        item.setQuantity(2);
        when(orderMapper.listOrderItems(ORDER_ID)).thenReturn(List.of(item));
        when(dishMapper.deductStock(10L, 2)).thenReturn(1);

        paymentService.mockSuccess(new PaymentMockSuccessRequest("PAY001"));
        verify(dishMapper).deductStock(10L, 2);

        // 3) Merchant accepts order
        doNothing().when(merchantAuthGuard).requireShopAccess(any(), eq(SHOP_ID));
        Order paidOrder = new Order();
        paidOrder.setId(ORDER_ID);
        paidOrder.setShopId(SHOP_ID);
        paidOrder.setStatus(1);
        when(orderMapper.findOrderById(ORDER_ID)).thenReturn(paidOrder);
        when(orderMapper.updateOrderStatusMerchant(ORDER_ID, 1, 2)).thenReturn(1);

        orderService.acceptOrder(MERCHANT_AUTH, ORDER_ID, null);
        verify(orderMapper).updateOrderStatusMerchant(ORDER_ID, 1, 2);

        // 4) Merchant delivers
        paidOrder.setStatus(2);
        when(orderMapper.updateOrderStatusMerchant(ORDER_ID, 2, 3)).thenReturn(1);
        orderService.deliveryOrder(MERCHANT_AUTH, ORDER_ID);

        // 5) Merchant finishes
        paidOrder.setStatus(3);
        when(orderMapper.updateOrderStatusMerchant(ORDER_ID, 3, 4)).thenReturn(1);
        orderService.finishOrder(MERCHANT_AUTH, ORDER_ID);

        verify(orderMapper).updateOrderStatusMerchant(ORDER_ID, 3, 4);
    }

    @Test
    void stockInsufficientShouldRollbackPayment() {
        when(paymentMapper.findPaymentByNo("PAY002")).thenReturn(Map.of(
                "orderId", 6001L, "payStatus", 0));
        when(paymentMapper.markPaymentSuccess("PAY002")).thenReturn(1);
        when(orderMapper.updateOrderPaySuccess(6001L)).thenReturn(1);

        OrderItem item = new OrderItem();
        item.setDishId(20L);
        item.setDishName("红烧肉");
        item.setQuantity(10);
        when(orderMapper.listOrderItems(6001L)).thenReturn(List.of(item));
        when(dishMapper.deductStock(20L, 10)).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> paymentService.mockSuccess(new PaymentMockSuccessRequest("PAY002")));
        assertEquals(400, ex.getCode());
        assertTrue(ex.getMessage().contains("红烧肉"));
    }
}
