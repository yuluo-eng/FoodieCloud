package com.example.springbootblank.rider.service;

import com.example.springbootblank.auth.security.JwtService;
import com.example.springbootblank.common.error.BusinessException;
import com.example.springbootblank.common.error.UnauthorizedException;
import com.example.springbootblank.rider.entity.Rider;
import com.example.springbootblank.rider.mapper.RiderMapper;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
public class RiderServiceImpl implements RiderService {

    private final JwtService jwtService;
    private final RiderMapper riderMapper;

    public RiderServiceImpl(JwtService jwtService, RiderMapper riderMapper) {
        this.jwtService = jwtService;
        this.riderMapper = riderMapper;
    }

    @Override
    public Map<String, Object> me(String authorizationHeader) {
        Rider rider = resolveRider(authorizationHeader);
        return Map.of(
                "id", rider.getId(),
                "username", rider.getUsername(),
                "realName", rider.getRealName(),
                "phone", rider.getPhone() == null ? "" : rider.getPhone(),
                "workStatus", rider.getWorkStatus()
        );
    }

    @Override
    public void updateWorkStatus(String authorizationHeader, String workStatus) {
        Rider rider = resolveRider(authorizationHeader);
        String normalized = (workStatus == null ? "" : workStatus.trim().toUpperCase());
        if (!"ONLINE".equals(normalized) && !"OFFLINE".equals(normalized)) {
            throw new BusinessException(400, "workStatus 仅支持 ONLINE/OFFLINE");
        }
        riderMapper.updateWorkStatus(rider.getId(), normalized);
    }

    private Rider resolveRider(String authorizationHeader) {
        String token = extractBearer(authorizationHeader);
        try {
            JwtService.JwtPrincipal principal = jwtService.parse(token);
            if (!JwtService.TYPE_RIDER.equals(principal.type())) {
                throw new UnauthorizedException("未登录或 Token 无效");
            }
            Rider rider = riderMapper.findById(principal.id());
            if (rider == null || (rider.getEnabled() != null && rider.getEnabled() == 0)) {
                throw new UnauthorizedException("未登录或 Token 无效");
            }
            return rider;
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
