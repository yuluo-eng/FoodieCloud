package com.example.springbootblank.auth.security;

import com.example.springbootblank.common.error.BusinessException;
import com.example.springbootblank.common.error.UnauthorizedException;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Set;

@Component
public class MerchantAuthGuard {

    private final JwtService jwtService;

    public MerchantAuthGuard(JwtService jwtService) {
        this.jwtService = jwtService;
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
