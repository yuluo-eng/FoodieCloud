package com.example.springbootblank.admin.service;

import com.example.springbootblank.admin.mapper.AdminMapper;
import com.example.springbootblank.order.entity.Order;
import com.example.springbootblank.shop.entity.Shop;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final AdminMapper adminMapper;

    public AdminService(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    public Map<String, Object> listUsers(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        long total = adminMapper.countAllUsers();
        List<Map<String, Object>> records = adminMapper.listUsers(pageSize, offset);
        return Map.of("total", total, "records", records);
    }

    public void toggleUserEnabled(long userId, int enabled) {
        adminMapper.updateUserEnabled(userId, enabled);
    }

    public Map<String, Object> listRiders(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        long total = adminMapper.countAllRiders();
        List<Map<String, Object>> records = adminMapper.listRiders(pageSize, offset);
        for (Map<String, Object> r : records) {
            Object riderId = r.get("id");
            if (riderId != null) {
                long delivered = adminMapper.countRiderDelivered(((Number) riderId).longValue());
                r.put("deliveredCount", delivered);
            }
        }
        return Map.of("total", total, "records", records);
    }

    public void toggleRiderEnabled(long riderId, int enabled) {
        adminMapper.updateRiderEnabled(riderId, enabled);
    }

    public Map<String, Object> listAllOrders(int page, int pageSize, Integer status, Long shopId) {
        int offset = (page - 1) * pageSize;
        long total = adminMapper.countAllOrders(status, shopId);
        List<Order> records = adminMapper.listAllOrders(status, shopId, pageSize, offset);
        return Map.of("total", total, "records", records);
    }

    public List<Shop> listShops() {
        return adminMapper.listShops();
    }

    public void toggleShopBusinessStatus(long shopId, int businessStatus) {
        adminMapper.updateShopBusinessStatus(shopId, businessStatus);
    }

    public Map<String, Object> dashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalOrders", adminMapper.countTotalOrders());
        BigDecimal revenue = adminMapper.sumTotalRevenue();
        data.put("totalRevenue", revenue != null ? revenue.toPlainString() : "0");
        data.put("activeRiders", adminMapper.countActiveRiders());
        data.put("totalUsers", adminMapper.countAllUsers());
        data.put("totalRiders", adminMapper.countAllRiders());
        data.put("totalShops", adminMapper.countShops());
        return data;
    }
}
