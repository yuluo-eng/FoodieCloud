package com.example.springbootblank.order.service;

import com.example.springbootblank.auth.security.JwtService;
import com.example.springbootblank.auth.security.MerchantAuthGuard;
import com.example.springbootblank.cart.mapper.CartMapper;
import com.example.springbootblank.common.error.BusinessException;
import com.example.springbootblank.log.service.OpLogService;
import com.example.springbootblank.order.entity.Order;
import com.example.springbootblank.order.mapper.OrderMapper;
import com.example.springbootblank.rider.entity.Rider;
import com.example.springbootblank.rider.mapper.RiderMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiderOrderFulfillmentServiceTest {

    private static final String RIDER_AUTH = "Bearer rider-token";

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
    void riderAcceptShouldSucceedWhenRowUpdated() {
        mockRiderPrincipal(10L);
        when(riderMapper.findById(10L)).thenReturn(onlineRider());
        when(orderMapper.riderAcceptOrder(100L, 10L)).thenReturn(1);

        assertDoesNotThrow(() -> orderService.riderAcceptOrder(RIDER_AUTH, 100L));
    }

    @Test
    void riderAcceptShouldBeIdempotentWhenAlreadyAcceptedBySameRider() {
        mockRiderPrincipal(10L);
        when(riderMapper.findById(10L)).thenReturn(onlineRider());
        when(orderMapper.riderAcceptOrder(100L, 10L)).thenReturn(0);
        Order o = new Order();
        o.setId(100L);
        o.setStatus(2);
        o.setRiderId(10L);
        when(orderMapper.findOrderById(100L)).thenReturn(o);

        assertDoesNotThrow(() -> orderService.riderAcceptOrder(RIDER_AUTH, 100L));
    }

    @Test
    void riderAcceptShouldReturn409WhenTakenByOtherRider() {
        mockRiderPrincipal(10L);
        when(riderMapper.findById(10L)).thenReturn(onlineRider());
        when(orderMapper.riderAcceptOrder(100L, 10L)).thenReturn(0);
        Order o = new Order();
        o.setId(100L);
        o.setStatus(2);
        o.setRiderId(99L);
        when(orderMapper.findOrderById(100L)).thenReturn(o);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.riderAcceptOrder(RIDER_AUTH, 100L));
        assertEquals(409, ex.getCode());
    }

    @Test
    void riderAcceptShouldReturn422WhenOrderNotInPool() {
        mockRiderPrincipal(10L);
        when(riderMapper.findById(10L)).thenReturn(onlineRider());
        when(orderMapper.riderAcceptOrder(100L, 10L)).thenReturn(0);
        Order o = new Order();
        o.setId(100L);
        o.setStatus(3);
        o.setRiderId(null);
        when(orderMapper.findOrderById(100L)).thenReturn(o);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.riderAcceptOrder(RIDER_AUTH, 100L));
        assertEquals(422, ex.getCode());
    }

    @Test
    void merchantDeliveryShouldReturn422WhenOrderAssignedToRider() {
        doNothing().when(merchantAuthGuard).requireShopAccess(any(), eq(1L));
        Order shopOrder = new Order();
        shopOrder.setId(200L);
        shopOrder.setShopId(1L);
        shopOrder.setRiderId(10L);
        when(orderMapper.findOrderById(200L)).thenReturn(shopOrder);
        when(orderMapper.updateOrderStatusMerchant(200L, 2, 3)).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.deliveryOrder("Bearer emp-token", 200L));
        assertEquals(422, ex.getCode());
    }

    private void mockRiderPrincipal(long riderId) {
        when(jwtService.parse("rider-token")).thenReturn(
                new JwtService.JwtPrincipal(JwtService.TYPE_RIDER, riderId, "r1", null));
    }

    private void mockEmployeePrincipal() {
        when(jwtService.parse("emp-token")).thenReturn(
                new JwtService.JwtPrincipal(JwtService.TYPE_EMPLOYEE, 1L, "admin", "SUPER_ADMIN"));
    }

    private static Rider onlineRider() {
        Rider r = new Rider();
        r.setId(10L);
        r.setEnabled(1);
        r.setWorkStatus("ONLINE");
        return r;
    }
}
