package com.example.springbootblank.order.service;

import com.example.springbootblank.auth.security.JwtService;
import com.example.springbootblank.cart.mapper.CartMapper;
import com.example.springbootblank.common.error.BusinessException;
import com.example.springbootblank.common.error.UnauthorizedException;
import com.example.springbootblank.order.dto.OrderCreateRequest;
import com.example.springbootblank.order.entity.Order;
import com.example.springbootblank.order.entity.OrderItem;
import com.example.springbootblank.order.mapper.OrderMapper;
import com.example.springbootblank.rider.entity.Rider;
import com.example.springbootblank.rider.mapper.RiderMapper;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    private final JwtService jwtService;
    private final CartMapper cartMapper;
    private final OrderMapper orderMapper;
    private final RiderMapper riderMapper;

    public OrderServiceImpl(JwtService jwtService, CartMapper cartMapper, OrderMapper orderMapper, RiderMapper riderMapper) {
        this.jwtService = jwtService;
        this.cartMapper = cartMapper;
        this.orderMapper = orderMapper;
        this.riderMapper = riderMapper;
    }

    @Override
    public Map<String, Object> createOrder(String authorization, OrderCreateRequest req) {
        Long userId = resolveUserId(authorization);
        Long shopId = req.shopId() == null ? 1L : req.shopId();

        List<Map<String, Object>> selected = cartMapper.listSelectedCartForOrder(userId);
        if (selected.isEmpty()) {
            throw new BusinessException(400, "购物车为空");
        }

        BigDecimal total = selected.stream()
                .map(item -> ((BigDecimal) item.get("unitPrice")).multiply(BigDecimal.valueOf((Integer) item.get("quantity"))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setOrderNo(buildOrderNo());
        order.setUserId(userId);
        order.setShopId(shopId);
        order.setTotalAmount(total);
        order.setRemark(req.remark());
        order.setStatus(0);
        order.setPayStatus(0);

        orderMapper.insertOrder(order);

        for (Map<String, Object> item : selected) {
            BigDecimal unitPrice = (BigDecimal) item.get("unitPrice");
            Integer qty = (Integer) item.get("quantity");

            OrderItem oi = new OrderItem();
            oi.setOrderId(order.getId());
            oi.setDishId(((Number) item.get("dishId")).longValue());
            oi.setDishName((String) item.get("dishName"));
            oi.setDishPrice(unitPrice);
            oi.setQuantity(qty);
            oi.setAmount(unitPrice.multiply(BigDecimal.valueOf(qty)));
            orderMapper.insertOrderItem(oi);
        }

        cartMapper.clearSelectedCart(userId);

        return Map.of(
                "orderId", order.getId(),
                "orderNo", order.getOrderNo(),
                "totalAmount", total,
                "status", order.getStatus()
        );
    }

    @Override
    public Map<String, Object> userOrders(String authorization, int page, int pageSize, Integer status) {
        Long userId = resolveUserId(authorization);
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(pageSize, 1);
        int offset = (safePage - 1) * safeSize;

        long total = orderMapper.countUserOrders(userId, status);
        List<Order> records = orderMapper.listUserOrders(userId, status, offset, safeSize);

        return Map.of(
                "page", safePage,
                "pageSize", safeSize,
                "total", total,
                "records", records
        );
    }

    @Override
    public Map<String, Object> userOrderDetail(String authorization, Long orderId) {
        Long userId = resolveUserId(authorization);
        Order order = orderMapper.findUserOrderById(userId, orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        List<OrderItem> items = orderMapper.listOrderItems(orderId);

        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("items", items);
        return data;
    }

    @Override
    public void cancelUserOrder(String authorization, Long orderId) {
        Long userId = resolveUserId(authorization);
        Order order = orderMapper.findUserOrderById(userId, orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        int rows = orderMapper.updateOrderCancelByUser(orderId);
        if (rows == 0) {
            throw new BusinessException(400, "仅未支付订单可取消");
        }
    }

    @Override
    public Map<String, Object> merchantOrders(String authorization, int page, int pageSize, Long shopId, Integer status) {
        resolveEmployee(authorization);

        int safePage = Math.max(page, 1);
        int safeSize = Math.max(pageSize, 1);
        int offset = (safePage - 1) * safeSize;

        long total = orderMapper.countMerchantOrders(shopId, status);
        List<Order> records = orderMapper.listMerchantOrders(shopId, status, offset, safeSize);

        return Map.of(
                "page", safePage,
                "pageSize", safeSize,
                "total", total,
                "records", records
        );
    }

    @Override
    public Map<String, Object> merchantOrderDetail(String authorization, Long shopId, Long orderId) {
        ensureMerchant(authorization);
        Order order = orderMapper.findOrderById(orderId);
        if (order == null || !shopId.equals(order.getShopId())) {
            throw new BusinessException(404, "订单不存在");
        }
        List<OrderItem> items = orderMapper.listOrderItems(orderId);
        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("items", items);
        return data;
    }

    @Override
    public void acceptOrder(String authorization, Long orderId) {
        ensureMerchant(authorization);
        int rows = orderMapper.updateOrderStatus(orderId, 1, 2);
        if (rows == 0) {
            throw new BusinessException(400, "仅已支付订单可接单");
        }
    }

    @Override
    public void deliveryOrder(String authorization, Long orderId) {
        ensureMerchant(authorization);
        int rows = orderMapper.updateOrderStatus(orderId, 2, 3);
        if (rows == 0) {
            throw new BusinessException(400, "仅已接单订单可发起配送");
        }
    }

    @Override
    public void finishOrder(String authorization, Long orderId) {
        ensureMerchant(authorization);
        int rows = orderMapper.updateOrderStatus(orderId, 3, 4);
        if (rows == 0) {
            throw new BusinessException(400, "仅配送中订单可完成");
        }
    }

    @Override
    public Map<String, Object> riderDispatchOrders(String authorization, int page, int pageSize) {
        Long riderId = resolveRiderId(authorization);
        ensureRiderOnline(riderId);
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(pageSize, 1);
        int offset = (safePage - 1) * safeSize;
        long total = orderMapper.countDispatchOrders();
        List<Order> records = orderMapper.listDispatchOrders(offset, safeSize);
        return Map.of(
                "page", safePage,
                "pageSize", safeSize,
                "total", total,
                "records", records
        );
    }

    @Override
    public Map<String, Object> riderCurrentOrders(String authorization) {
        Long riderId = resolveRiderId(authorization);
        List<Order> records = orderMapper.listRiderCurrentOrders(riderId);
        return Map.of("records", records);
    }

    @Override
    public void riderAcceptOrder(String authorization, Long orderId) {
        Long riderId = resolveRiderId(authorization);
        ensureRiderOnline(riderId);
        int rows = orderMapper.riderAcceptOrder(orderId, riderId);
        if (rows == 0) {
            throw new BusinessException(400, "订单已被接单或当前状态不可接单");
        }
    }

    @Override
    public void riderArriveShop(String authorization, Long orderId) {
        Long riderId = resolveRiderId(authorization);
        int rows = orderMapper.riderArriveShop(orderId, riderId);
        if (rows == 0) {
            throw new BusinessException(400, "仅已接单状态可操作到店");
        }
    }

    @Override
    public void riderPickup(String authorization, Long orderId) {
        Long riderId = resolveRiderId(authorization);
        int rows = orderMapper.riderPickup(orderId, riderId);
        if (rows == 0) {
            throw new BusinessException(400, "仅到店状态可操作取餐");
        }
    }

    @Override
    public void riderDelivered(String authorization, Long orderId) {
        Long riderId = resolveRiderId(authorization);
        int rows = orderMapper.riderDelivered(orderId, riderId);
        if (rows == 0) {
            throw new BusinessException(400, "仅取餐状态可操作送达");
        }
    }

    private String buildOrderNo() {
        return "YSH" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    private void ensureMerchant(String authorizationHeader) {
        resolveEmployee(authorizationHeader);
    }

    private JwtService.JwtPrincipal resolveEmployee(String authorizationHeader) {
        String token = extractBearer(authorizationHeader);
        try {
            var principal = jwtService.parse(token);
            if (!JwtService.TYPE_EMPLOYEE.equals(principal.type())) {
                throw new UnauthorizedException("未登录或 Token 无效");
            }
            return principal;
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("未登录或 Token 无效");
        }
    }

    private Long resolveUserId(String authorizationHeader) {
        String token = extractBearer(authorizationHeader);
        try {
            var principal = jwtService.parse(token);
            if (!JwtService.TYPE_USER.equals(principal.type())) {
                throw new UnauthorizedException("未登录或 Token 无效");
            }
            return principal.id();
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("未登录或 Token 无效");
        }
    }

    private Long resolveRiderId(String authorizationHeader) {
        String token = extractBearer(authorizationHeader);
        try {
            var principal = jwtService.parse(token);
            if (!JwtService.TYPE_RIDER.equals(principal.type())) {
                throw new UnauthorizedException("未登录或 Token 无效");
            }
            return principal.id();
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("未登录或 Token 无效");
        }
    }

    private void ensureRiderOnline(Long riderId) {
        Rider rider = riderMapper.findById(riderId);
        if (rider == null || (rider.getEnabled() != null && rider.getEnabled() == 0)) {
            throw new UnauthorizedException("未登录或 Token 无效");
        }
        if (!"ONLINE".equalsIgnoreCase(rider.getWorkStatus())) {
            throw new BusinessException(400, "骑手当前为离线状态，无法接单");
        }
    }

    private static String extractBearer(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("未登录或 Token 无效");
        }
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (!StringUtils.hasText(token)) {
            throw new UnauthorizedException("未登录或 Token 无效");
        }
        return token;
    }
}
