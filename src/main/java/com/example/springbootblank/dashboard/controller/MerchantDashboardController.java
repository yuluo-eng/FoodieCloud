package com.example.springbootblank.dashboard.controller;

import com.example.springbootblank.auth.mapper.AuthMapper;
import com.example.springbootblank.auth.security.MerchantAuthGuard;
import com.example.springbootblank.common.api.ApiResponse;
import com.example.springbootblank.common.error.BusinessException;
import com.example.springbootblank.dish.mapper.DishMapper;
import com.example.springbootblank.employee.entity.Employee;
import com.example.springbootblank.employee.mapper.EmployeeMapper;
import com.example.springbootblank.order.mapper.OrderMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant/dashboard")
public class MerchantDashboardController {

    private final OrderMapper orderMapper;
    private final EmployeeMapper employeeMapper;
    private final DishMapper dishMapper;
    private final AuthMapper authMapper;
    private final MerchantAuthGuard merchantAuthGuard;

    public MerchantDashboardController(OrderMapper orderMapper,
                                        EmployeeMapper employeeMapper,
                                        DishMapper dishMapper,
                                        AuthMapper authMapper,
                                        MerchantAuthGuard merchantAuthGuard) {
        this.orderMapper = orderMapper;
        this.employeeMapper = employeeMapper;
        this.dishMapper = dishMapper;
        this.authMapper = authMapper;
        this.merchantAuthGuard = merchantAuthGuard;
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        var principal = merchantAuthGuard.requireEmployeeRole(authorization, "SUPER_ADMIN", "SHOP_MANAGER", "STAFF");

        Employee employee = authMapper.findEmployeeByIdWithRole(principal.id());
        if (employee == null || employee.getShopId() == null) {
            // 数据脏时不要直接打断页面渲染（前端统计卡片兜底为 0）
            Map<String, Object> data = new HashMap<>();
            data.put("totalOrders", 0L);
            data.put("totalRevenue", 0);
            data.put("totalEmployees", 0L);
            data.put("totalDishes", 0L);
            return ApiResponse.ok(data);
        }
        Long shopId = employee.getShopId();

        // 统计“今天”的口径：pay_time 落在本地时区今天 00:00~明天 00:00
        ZoneId zone = ZoneId.of("Asia/Shanghai");
        LocalDate today = LocalDate.now(zone);
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        long totalOrders = orderMapper.countMerchantOrdersPaidToday(shopId, start, end);
        BigDecimal totalRevenue = orderMapper.sumMerchantRevenuePaidToday(shopId, start, end);

        long totalEmployees = employeeMapper.countEmployees(shopId, null, 1);
        long totalDishes = dishMapper.countMerchantDishes(shopId, null, null, 1);

        Map<String, Object> data = new HashMap<>();
        data.put("totalOrders", totalOrders);
        data.put("totalRevenue", totalRevenue == null ? 0 : totalRevenue);
        data.put("totalEmployees", totalEmployees);
        data.put("totalDishes", totalDishes);

        return ApiResponse.ok(data);
    }
}

