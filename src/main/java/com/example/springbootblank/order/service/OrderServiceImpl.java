package com.example.springbootblank.order.service;

import com.example.springbootblank.auth.security.JwtService;
import com.example.springbootblank.auth.security.MerchantAuthGuard;
import com.example.springbootblank.cart.mapper.CartMapper;
import com.example.springbootblank.common.error.BusinessException;
import com.example.springbootblank.common.error.UnauthorizedException;
import com.example.springbootblank.log.service.OpLogService;
import com.example.springbootblank.order.dto.OrderCreateRequest;
import com.example.springbootblank.order.entity.Order;
import com.example.springbootblank.order.entity.OrderItem;
import com.example.springbootblank.order.mapper.OrderMapper;
import com.example.springbootblank.rider.entity.Rider;
import com.example.springbootblank.rider.mapper.RiderMapper;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final MerchantAuthGuard merchantAuthGuard;
    private final OpLogService opLogService;

    public OrderServiceImpl(JwtService jwtService, CartMapper cartMapper, OrderMapper orderMapper,
                            RiderMapper riderMapper, MerchantAuthGuard merchantAuthGuard,
                            OpLogService opLogService) {
        this.jwtService = jwtService;
        this.cartMapper = cartMapper;
        this.orderMapper = orderMapper;
        this.riderMapper = riderMapper;
        this.merchantAuthGuard = merchantAuthGuard;
        this.opLogService = opLogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createOrder(String authorization, OrderCreateRequest req) {
        Long userId = resolveUserId(authorization);
        if (req.shopId() == null) {
            throw new BusinessException(400, "shopId 不能为空");
        }
        Long shopId = req.shopId();

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

        int cleared = cartMapper.clearSelectedCart(userId);
        if (cleared <= 0) {
            throw new BusinessException(409, "购物车状态已变化，请重试下单");
        }

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
        merchantAuthGuard.requireShopAccess(authorization, shopId);

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
        merchantAuthGuard.requireShopAccess(authorization, shopId);
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
    public void acceptOrder(String authorization, Long orderId, String deliveryMode) {
        Long shopId = merchantAuthGuard.resolveShopId(authorization);
        ensureOrderBelongsToShop(orderId, shopId);

        if ("RIDER".equalsIgnoreCase(deliveryMode)) {
            Order order = orderMapper.findOrderById(orderId);
            if (order == null) {
                throw new BusinessException(404, "订单不存在");
            }
            if (order.getStatus() != 1) {
                throw new BusinessException(400, "仅已支付订单可操作");
            }
            if (order.getRiderId() != null) {
                throw new BusinessException(422, "该订单已被骑手接单");
            }
            opLogService.log("EMPLOYEE", null, "ORDER", "ACCEPT_RIDER_MODE",
                    "订单" + orderId + " 商家确认接单并推送至骑手抢单池");
            return;
        }

        applyMerchantStatusTransition(
                orderId,
                1,
                2,
                "仅已支付订单可接单",
                "该订单由骑手履约，请勿在商家端接单"
        );
    }

    @Override
    public void deliveryOrder(String authorization, Long orderId) {
        Long shopId = merchantAuthGuard.resolveShopId(authorization);
        ensureOrderBelongsToShop(orderId, shopId);
        applyMerchantStatusTransition(
                orderId,
                2,
                3,
                "仅已接单订单可发起配送",
                "该订单由骑手配送，请在骑手端完成到店/取餐/送达"
        );
    }

    @Override
    public void finishOrder(String authorization, Long orderId) {
        Long shopId = merchantAuthGuard.resolveShopId(authorization);
        ensureOrderBelongsToShop(orderId, shopId);
        applyMerchantStatusTransition(
                orderId,
                3,
                4,
                "仅配送中订单可完成",
                "该订单由骑手配送，请在骑手端确认送达"
        );
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
    @Transactional(rollbackFor = Exception.class)
    public void riderAcceptOrder(String authorization, Long orderId) {
        Long riderId = resolveRiderId(authorization);
        ensureRiderOnline(riderId);
        int rows = orderMapper.riderAcceptOrder(orderId, riderId);
        if (rows == 1) {
            return;
        }
        Order o = orderMapper.findOrderById(orderId);
        if (o == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (o.getRiderId() != null && o.getRiderId().equals(riderId) && o.getStatus() != null && o.getStatus() == 2) {
            return;
        }
        if (o.getRiderId() != null && !o.getRiderId().equals(riderId)) {
            throw new BusinessException(409, "订单已被其他骑手接单");
        }
        throw new BusinessException(422, "当前订单状态不允许接单");
    }

    @Override
    public void riderArriveShop(String authorization, Long orderId) {
        Long riderId = resolveRiderId(authorization);
        int rows = orderMapper.riderArriveShop(orderId, riderId);
        onRiderFulfillmentMismatch(
                rows,
                orderId,
                riderId,
                "当前订单状态不允许到店签到",
                "非当前骑手订单"
        );
    }

    @Override
    public void riderPickup(String authorization, Long orderId) {
        Long riderId = resolveRiderId(authorization);
        int rows = orderMapper.riderPickup(orderId, riderId);
        onRiderFulfillmentMismatch(
                rows,
                orderId,
                riderId,
                "请先到店签到后再取餐",
                "非当前骑手订单"
        );
    }

    @Override
    public void riderDelivered(String authorization, Long orderId) {
        Long riderId = resolveRiderId(authorization);
        int rows = orderMapper.riderDelivered(orderId, riderId);
        onRiderFulfillmentMismatch(
                rows,
                orderId,
                riderId,
                "请先取餐后再确认送达",
                "非当前骑手订单"
        );
    }

    private void applyMerchantStatusTransition(
            Long orderId,
            int fromStatus,
            int toStatus,
            String illegalLegMessage,
            String riderFulfillmentMessage
    ) {
        int rows = orderMapper.updateOrderStatusMerchant(orderId, fromStatus, toStatus);
        if (rows > 0) {
            opLogService.log("EMPLOYEE", null, "ORDER", "STATUS_CHANGE",
                    "订单" + orderId + " 状态 " + fromStatus + " → " + toStatus);
            return;
        }
        Order o = orderMapper.findOrderById(orderId);
        if (o == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (o.getRiderId() != null) {
            throw new BusinessException(422, riderFulfillmentMessage);
        }
        throw new BusinessException(400, illegalLegMessage);
    }

    /**
     * 履约步骤失败时：区分不存在、越权骑手、状态非法（422）。
     */
    private void onRiderFulfillmentMismatch(
            int rows,
            Long orderId,
            Long riderId,
            String illegalStateMessage,
            String wrongRiderMessage
    ) {
        if (rows > 0) {
            return;
        }
        Order o = orderMapper.findOrderById(orderId);
        if (o == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (o.getRiderId() == null || !o.getRiderId().equals(riderId)) {
            throw new BusinessException(403, wrongRiderMessage);
        }
        throw new BusinessException(422, illegalStateMessage);
    }

    private void ensureOrderBelongsToShop(Long orderId, Long shopId) {
        Order order = orderMapper.findOrderById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (!shopId.equals(order.getShopId())) {
            throw new BusinessException(403, "无权操作其他店铺的订单");
        }
    }

    private String buildOrderNo() {
        return "YSH" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    private Long resolveUserId(String authorizationHeader) {
        String token = extractBearer(authorizationHeader);
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

    private Long resolveRiderId(String authorizationHeader) {
        String token = extractBearer(authorizationHeader);
        try {
            var principal = jwtService.parse(token);
            if (!JwtService.TYPE_RIDER.equals(principal.type())) {
                throw new BusinessException(403, "无权限");
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
