package com.example.springbootblank.payment.mapper;

import com.example.springbootblank.payment.entity.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface PaymentMapper {

    int insertPayment(PaymentRecord paymentRecord);

    Map<String, Object> findPaymentByNo(@Param("paymentNo") String paymentNo);

    Map<String, Object> findPaymentByNoForUser(@Param("paymentNo") String paymentNo, @Param("userId") Long userId);

    int markPaymentSuccess(@Param("paymentNo") String paymentNo);
}
