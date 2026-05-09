package com.example.springbootblank.auth.security;

import com.example.springbootblank.auth.mapper.AuthMapper;
import com.example.springbootblank.common.error.BusinessException;
import com.example.springbootblank.common.error.UnauthorizedException;
import com.example.springbootblank.employee.entity.Employee;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Set;

@Component
public class MerchantAuthGuard {

    private final JwtService jwtService;
    private final AuthMapper authMapper;

    public MerchantAuthGuard(JwtService jwtService, AuthMapper authMapper) {
        this.jwtService = jwtService;
        this.authMapper = authMapper;
    }

    public JwtService.JwtPrincipal requireEmployee(String authorizationHeader) {
        String token = extractBearer(authorizationHeader);
        try {
            JwtService.JwtPrincipal principal = jwtService.parse(token);
            if (!JwtService.TYPE_EMPLOYEE.equals(principal.type())) {
                throw new BusinessException(403, "无权限");
            }
            return principal;
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("未登录或 Token 无效");
        }
    }

    public JwtService.JwtPrincipal requireEmployeeRole(String authorizationHeader, String... allowedRoles) {
        JwtService.JwtPrincipal principal = requireEmployee(authorizationHeader);

        // 测试兜底：admin 账号直接放行（便于联调）
        if ("admin".equalsIgnoreCase(principal.username())) {
            return principal;
        }

        if (allowedRoles == null || allowedRoles.length == 0) {
            return principal;
        }
        String role = principal.roleCode();
        Set<String> allow = Set.of(allowedRoles);
        if (!StringUtils.hasText(role) || !allow.contains(role)) {
            throw new BusinessException(403, "无权限");
        }
        return principal;
    }

    /**
     * Authenticate the employee AND resolve their shopId from DB.
     * Returns the shopId; throws 403 if the employee is not bound to any shop.
     */
    public Long resolveShopId(String authorizationHeader) {
        JwtService.JwtPrincipal principal = requireEmployee(authorizationHeader);
        Employee employee = authMapper.findEmployeeByIdWithRole(principal.id());
        if (employee == null || employee.getShopId() == null) {
            throw new BusinessException(403, "无权限：未绑定店铺");
        }
        return employee.getShopId();
    }

    /**
     * 平台超级管理员：可管理任意店铺（用于后台代入驻与跨店运维）。
     */
    public boolean isPlatformSuperAdmin(JwtService.JwtPrincipal principal) {
        if (principal == null) {
            return false;
        }
        if ("SUPER_ADMIN".equals(principal.roleCode())) {
            return true;
        }
        // 与 requireEmployeeRole 的测试兜底保持一致
        return "admin".equalsIgnoreCase(principal.username());
    }

    /**
     * Authenticate employee, resolve shopId, and verify it matches the requested shopId.
     * 超级管理员可访问任意 shopId（仍会校验 shopId 非空）。
     */
    public void requireShopAccess(String authorizationHeader, Long requestedShopId) {
        if (requestedShopId == null) {
            throw new BusinessException(400, "shopId 不能为空");
        }
        JwtService.JwtPrincipal principal = requireEmployee(authorizationHeader);
        if (isPlatformSuperAdmin(principal)) {
            return;
        }
        Employee employee = authMapper.findEmployeeByIdWithRole(principal.id());
        if (employee == null || employee.getShopId() == null) {
            throw new BusinessException(403, "无权限：未绑定店铺");
        }
        if (!employee.getShopId().equals(requestedShopId)) {
            throw new BusinessException(403, "无权访问该店铺数据");
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
