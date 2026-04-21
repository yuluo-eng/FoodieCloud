package com.example.springbootblank.user;

import com.example.springbootblank.auth.security.JwtService;
import com.example.springbootblank.auth.security.UserAuthGuard;
import com.example.springbootblank.common.api.ApiResponse;
import com.example.springbootblank.user.dto.UserProfileResponse;
import com.example.springbootblank.user.dto.UserProfileUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/profile")
public class UserProfileController {

    private final UserAuthGuard userAuthGuard;
    private final UserProfileService userProfileService;

    public UserProfileController(UserAuthGuard userAuthGuard, UserProfileService userProfileService) {
        this.userAuthGuard = userAuthGuard;
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public ApiResponse<UserProfileResponse> getProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        JwtService.JwtPrincipal p = userAuthGuard.requireUser(authorization);
        return ApiResponse.ok(userProfileService.getProfile(p.id()));
    }

    @PutMapping
    public ApiResponse<Void> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody UserProfileUpdateRequest req
    ) {
        JwtService.JwtPrincipal p = userAuthGuard.requireUser(authorization);
        userProfileService.updateProfile(p.id(), req);
        return ApiResponse.ok();
    }
}
