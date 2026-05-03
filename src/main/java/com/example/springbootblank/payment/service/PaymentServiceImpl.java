package com.example.springbootblank.payment.service;

import com.example.springbootblank.auth.security.JwtService;
import com.example.springbootblank.common.error.BusinessException;
import com.example.springbootblank.common.error.UnauthorizedException;
import com.example.springbootblank.dish.mapper.DishMapper;
import com.example.springbootblank.log.service.OpLogService;
import com.example.springbootblank.order.entity.Order;
import com.example.springbootblank.order.entity.OrderItem;
import com.example.springbootblank.order.mapper.OrderMapper;
import com.example.springbootblank.payment.dto.PaymentCreateRequest;
import com.example.springbootblank.payment.dto.PaymentMockSuccessRequest;
import com.example.springbootblank.payment.entity.PaymentRecord;
import com.example.springbootblank.payment.mapper.PaymentMapper;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final JwtService jwtService;
    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final DishMapper dishMapper;
    private final OpLogService opLogService;

    public PaymentServiceImpl(JwtService jwtService, PaymentMapper paymentMapper,
                              OrderMapper orderMapper, DishMapper dishMapper,
                              OpLogService opLogService) {
        this.jwtService = jwtService;
        this.paymentMapper = paymentMapper;
        this.orderMapper = orderMapper;
        this.dishMapper = dishMapper;
        this.opLogService = opLogService;
    }

    @Override
    public Map<String, Object> create(String authorization, PaymentCreateRequest req) {
        Long userId = resolveUserId(authorization);
        Order order = orderMapper.findUserOrderById(userId, req.orderId());
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (order.getPayStatus() != null && order.getPayStatus() == 1) {
            throw new BusinessException(400, "订单已支付");
        }

        PaymentRecord record = new PaymentRecord();
        record.setOrderId(order.getId());
        record.setPaymentNo(buildPaymentNo());
        record.setPayChannel(StringUtils.hasText(req.payChannel()) ? req.payChannel() : "MOCK");
        record.setPayAmount(order.getTotalAmount());
        record.setPayStatus(0);
        paymentMapper.insertPayment(record);

        return Map.of(
                "paymentNo", record.getPaymentNo(),
                "payAmount", record.getPayAmount(),
                "payStatus", record.getPayStatus()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mockSuccess(PaymentMockSuccessRequest req) {
        Map<String, Object> payment = paymentMapper.findPaymentByNo(req.paymentNo());
        if (payment == null) {
            throw new BusinessException(404, "支付单不存在");
        }

        Integer payStatus = payment.get("payStatus") == null ? null : ((Number) payment.get("payStatus")).intValue();
        if (payStatus != null && payStatus == 1) {
            return;
        }

        int paymentRows = paymentMapper.markPaymentSuccess(req.paymentNo());
        if (paymentRows == 0) {
            throw new BusinessException(409, "支付状态已变化，请刷新后重试");
        }

        Long orderId = ((Number) payment.get("orderId")).longValue();
        int orderRows = orderMapper.updateOrderPaySuccess(orderId);
        if (orderRows == 0) {
            throw new BusinessException(409, "订单状态已变化，请刷新后重试");
        }

        List<OrderItem> items = orderMapper.listOrderItems(orderId);
        for (OrderItem item : items) {
            int deducted = dishMapper.deductStock(item.getDishId(), item.getQuantity());
            if (deducted == 0) {
                throw new BusinessException(400, "菜品「" + item.getDishName() + "」库存不足");
            }
        }

        opLogService.log("USER", null, "PAYMENT", "PAY_SUCCESS",
                "支付成功: " + req.paymentNo() + ", 订单ID: " + orderId);
    }

    @Override
    public Map<String, Object> status(String authorization, String paymentNo) {
        Long userId = resolveUserId(authorization);
        Map<String, Object> payment = paymentMapper.findPaymentByNoForUser(paymentNo, userId);
        if (payment == null) {
            throw new BusinessException(404, "支付单不存在");
        }
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("paymentNo", payment.get("paymentNo"));
        data.put("payAmount", payment.get("payAmount"));
        data.put("payStatus", payment.get("payStatus"));
        data.put("paidTime", payment.get("paidTime"));
        return data;
    }

    private String buildPaymentNo() {
        return "PAY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    private Long resolveUserId(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("未登录或 Token 无效");
        }
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        try {
            var principal = jwtService.parse(token);
            if (!JwtService.TYPE_USER.equals(principal.type())) {
                throw new BusinessException(403, "无权限");
            }
            return principal.id();
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("未登录或 Token 无效");
        }
    }
}
