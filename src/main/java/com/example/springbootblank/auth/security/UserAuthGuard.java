package com.example.springbootblank.auth.security;

import com.example.springbootblank.common.error.UnauthorizedException;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class UserAuthGuard {

    private final JwtService jwtService;

    public UserAuthGuard(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public JwtService.JwtPrincipal requireUser(String authorizationHeader) {
        String token = extractBearer(authorizationHeader);
        try {
            JwtService.JwtPrincipal principal = jwtService.parse(token);
            if (!JwtService.TYPE_USER.equals(principal.type())) {
                throw new UnauthorizedException("未登录或 Token 无效");
            }
            return principal;
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("未登录或 Token 无效");
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
