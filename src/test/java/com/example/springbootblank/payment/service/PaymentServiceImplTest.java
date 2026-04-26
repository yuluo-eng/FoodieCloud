package com.example.springbootblank.payment.service;

import com.example.springbootblank.auth.security.JwtService;
import com.example.springbootblank.common.error.BusinessException;
import com.example.springbootblank.order.mapper.OrderMapper;
import com.example.springbootblank.payment.dto.PaymentMockSuccessRequest;
import com.example.springbootblank.payment.mapper.PaymentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void mockSuccessShouldThrowWhenPaymentNotFound() {
        when(paymentMapper.findPaymentByNo("P100")).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> paymentService.mockSuccess(new PaymentMockSuccessRequest("P100")));

        assertEquals(404, ex.getCode());
        verify(paymentMapper, never()).markPaymentSuccess("P100");
        verify(orderMapper, never()).updateOrderPaySuccess(1L);
    }

    @Test
    void mockSuccessShouldBeIdempotentWhenAlreadyPaid() {
        when(paymentMapper.findPaymentByNo("P101")).thenReturn(Map.of(
                "orderId", 3001L,
                "payStatus", 1
        ));

        paymentService.mockSuccess(new PaymentMockSuccessRequest("P101"));

        verify(paymentMapper, never()).markPaymentSuccess("P101");
        verify(orderMapper, never()).updateOrderPaySuccess(3001L);
    }

    @Test
    void mockSuccessShouldThrowWhenPaymentRowNotUpdated() {
        when(paymentMapper.findPaymentByNo("P102")).thenReturn(Map.of(
                "orderId", 3002L,
                "payStatus", 0
        ));
        when(paymentMapper.markPaymentSuccess("P102")).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> paymentService.mockSuccess(new PaymentMockSuccessRequest("P102")));

        assertEquals(409, ex.getCode());
        verify(orderMapper, never()).updateOrderPaySuccess(3002L);
    }

    @Test
    void mockSuccessShouldThrowWhenOrderRowNotUpdated() {
        when(paymentMapper.findPaymentByNo("P103")).thenReturn(Map.of(
                "orderId", 3003L,
                "payStatus", 0
        ));
        when(paymentMapper.markPaymentSuccess("P103")).thenReturn(1);
        when(orderMapper.updateOrderPaySuccess(3003L)).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> paymentService.mockSuccess(new PaymentMockSuccessRequest("P103")));

        assertEquals(409, ex.getCode());
    }

    @Test
    void mockSuccessShouldUpdatePaymentAndOrder() {
        when(paymentMapper.findPaymentByNo("P104")).thenReturn(Map.of(
                "orderId", 3004L,
                "payStatus", 0
        ));
        when(paymentMapper.markPaymentSuccess("P104")).thenReturn(1);
        when(orderMapper.updateOrderPaySuccess(3004L)).thenReturn(1);

        paymentService.mockSuccess(new PaymentMockSuccessRequest("P104"));

        verify(paymentMapper).markPaymentSuccess("P104");
        verify(orderMapper).updateOrderPaySuccess(3004L);
    }
}

