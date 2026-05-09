package com.example.springbootblank.admin.service;

import com.example.springbootblank.admin.dto.AdminEmployeeBootstrapRequest;
import com.example.springbootblank.admin.dto.AdminShopCreateRequest;
import com.example.springbootblank.admin.mapper.AdminMapper;
import com.example.springbootblank.common.error.BusinessException;
import com.example.springbootblank.employee.entity.Employee;
import com.example.springbootblank.employee.mapper.EmployeeMapper;
import com.example.springbootblank.order.entity.Order;
import com.example.springbootblank.shop.entity.Shop;
import com.example.springbootblank.shop.mapper.ShopMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final AdminMapper adminMapper;
    private final ShopMapper shopMapper;
    private final EmployeeMapper employeeMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminService(AdminMapper adminMapper,
                        ShopMapper shopMapper,
                        EmployeeMapper employeeMapper,
                        BCryptPasswordEncoder passwordEncoder) {
        this.adminMapper = adminMapper;
        this.shopMapper = shopMapper;
        this.employeeMapper = employeeMapper;
        this.passwordEncoder = passwordEncoder;
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

    public Map<String, Object> createShop(AdminShopCreateRequest req) {
        if (req == null || !StringUtils.hasText(req.shopName())) {
            throw new BusinessException(400, "店铺名称不能为空");
        }
        int bs = req.businessStatus() == null ? 1 : req.businessStatus();
        if (bs != 0 && bs != 1) {
            throw new BusinessException(400, "businessStatus 仅支持 0/1");
        }
        Shop shop = new Shop();
        shop.setShopName(req.shopName().trim());
        shop.setAddress(StringUtils.hasText(req.address()) ? req.address().trim() : null);
        shop.setPhone(StringUtils.hasText(req.phone()) ? req.phone().trim() : null);
        shop.setNotice(StringUtils.hasText(req.notice()) ? req.notice().trim() : null);
        shop.setBusinessStatus(bs);
        adminMapper.insertShop(shop);
        return Map.of(
                "id", shop.getId(),
                "shopName", shop.getShopName(),
                "businessStatus", shop.getBusinessStatus()
        );
    }

    public Map<String, Object> bootstrapShopEmployee(long shopId, AdminEmployeeBootstrapRequest req) {
        Shop shop = shopMapper.findById(shopId);
        if (shop == null) {
            throw new BusinessException(404, "店铺不存在");
        }
        if (req == null || !StringUtils.hasText(req.username()) || !StringUtils.hasText(req.password())) {
            throw new BusinessException(400, "账号与密码不能为空");
        }
        String username = req.username().trim();
        if (employeeMapper.countByUsername(username) > 0) {
            throw new BusinessException(400, "账号已存在");
        }
        String phone = StringUtils.hasText(req.phone()) ? req.phone().trim() : null;
        if (phone != null && employeeMapper.countByPhone(phone) > 0) {
            throw new BusinessException(400, "手机号已存在");
        }
        long roleId = req.roleId() != null ? req.roleId() : 2L;

        Employee employee = new Employee();
        employee.setUsername(username);
        employee.setPassword(passwordEncoder.encode(req.password()));
        employee.setRealName(StringUtils.hasText(req.realName()) ? req.realName().trim() : username);
        employee.setPhone(phone);
        employee.setShopId(shopId);
        employee.setRoleId(roleId);
        employee.setEnabled(1);
        employeeMapper.insertEmployee(employee);
        return Map.of("id", employee.getId(), "username", employee.getUsername(), "shopId", shopId, "roleId", roleId);
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
