package com.example.springbootblank.user;

import com.example.springbootblank.auth.security.UserAuthGuard;
import com.example.springbootblank.common.api.ApiResponse;
import com.example.springbootblank.user.dto.GeocodeReverseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/user/geocode")
public class UserGeocodeController {

    private final UserAuthGuard userAuthGuard;
    private final GeocodeService geocodeService;

    public UserGeocodeController(UserAuthGuard userAuthGuard, GeocodeService geocodeService) {
        this.userAuthGuard = userAuthGuard;
        this.geocodeService = geocodeService;
    }

    /**
     * 根据经纬度逆解析为可读地址（需登录，防止滥用公网服务）。
     */
    @GetMapping("/reverse")
    public ApiResponse<GeocodeReverseResponse> reverse(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude
    ) {
        userAuthGuard.requireUser(authorization);
        return ApiResponse.ok(geocodeService.reverse(latitude, longitude));
    }
}
