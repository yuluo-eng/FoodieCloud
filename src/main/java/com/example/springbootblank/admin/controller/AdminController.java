package com.example.springbootblank.admin.controller;

import com.example.springbootblank.admin.dto.AdminEmployeeBootstrapRequest;
import com.example.springbootblank.admin.dto.AdminShopCreateRequest;
import com.example.springbootblank.admin.service.AdminService;
import com.example.springbootblank.auth.security.MerchantAuthGuard;
import com.example.springbootblank.common.api.ApiResponse;
import com.example.springbootblank.shop.entity.Shop;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final MerchantAuthGuard merchantAuthGuard;

    public AdminController(AdminService adminService, MerchantAuthGuard merchantAuthGuard) {
        this.adminService = adminService;
        this.merchantAuthGuard = merchantAuthGuard;
    }

    private void requireSuperAdmin(String auth) {
        merchantAuthGuard.requireEmployeeRole(auth, "SUPER_ADMIN");
    }

    @GetMapping("/users")
    public ApiResponse<Map<String, Object>> listUsers(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        requireSuperAdmin(auth);
        return ApiResponse.ok(adminService.listUsers(page, pageSize));
    }

    @PatchMapping("/users/{id}/toggle")
    public ApiResponse<Void> toggleUser(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long id,
            @RequestParam int enabled) {
        requireSuperAdmin(auth);
        adminService.toggleUserEnabled(id, enabled);
        return ApiResponse.ok();
    }

    @GetMapping("/riders")
    public ApiResponse<Map<String, Object>> listRiders(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        requireSuperAdmin(auth);
        return ApiResponse.ok(adminService.listRiders(page, pageSize));
    }

    @PatchMapping("/riders/{id}/toggle")
    public ApiResponse<Void> toggleRider(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long id,
            @RequestParam int enabled) {
        requireSuperAdmin(auth);
        adminService.toggleRiderEnabled(id, enabled);
        return ApiResponse.ok();
    }

    @GetMapping("/orders")
    public ApiResponse<Map<String, Object>> listOrders(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long shopId) {
        requireSuperAdmin(auth);
        return ApiResponse.ok(adminService.listAllOrders(page, pageSize, status, shopId));
    }

    @GetMapping("/shops")
    public ApiResponse<List<Shop>> listShops(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        requireSuperAdmin(auth);
        return ApiResponse.ok(adminService.listShops());
    }

    @PostMapping("/shops")
    public ApiResponse<Map<String, Object>> createShop(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody AdminShopCreateRequest req
    ) {
        requireSuperAdmin(auth);
        return ApiResponse.ok(adminService.createShop(req));
    }

    @PostMapping("/shops/{shopId}/employees/bootstrap")
    public ApiResponse<Map<String, Object>> bootstrapShopEmployee(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long shopId,
            @RequestBody AdminEmployeeBootstrapRequest req
    ) {
        requireSuperAdmin(auth);
        return ApiResponse.ok(adminService.bootstrapShopEmployee(shopId, req));
    }

    @PatchMapping("/shops/{id}/toggle")
    public ApiResponse<Void> toggleShop(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long id,
            @RequestParam int businessStatus) {
        requireSuperAdmin(auth);
        adminService.toggleShopBusinessStatus(id, businessStatus);
        return ApiResponse.ok();
    }

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        requireSuperAdmin(auth);
        return ApiResponse.ok(adminService.dashboard());
    }
}
