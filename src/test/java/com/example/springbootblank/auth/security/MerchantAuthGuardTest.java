package com.example.springbootblank.auth.security;

import com.example.springbootblank.auth.config.JwtProperties;
import com.example.springbootblank.common.error.BusinessException;
import com.example.springbootblank.common.error.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MerchantAuthGuardTest {

    private JwtService jwtService;
    private MerchantAuthGuard merchantAuthGuard;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("unit-test-secret-key-should-be-long-enough-123456");
        props.setExpirationMs(86_400_000L);
        jwtService = new JwtService(props);
        merchantAuthGuard = new MerchantAuthGuard(jwtService);
    }

    @Test
    void employeeWithManagerRoleShouldPass() {
        String token = jwtService.createEmployeeToken(1L, "manager", "SHOP_MANAGER");
        String auth = "Bearer " + token;

        assertDoesNotThrow(() -> merchantAuthGuard.requireEmployeeRole(auth, "SUPER_ADMIN", "SHOP_MANAGER"));
    }

    @Test
    void employeeWithStaffRoleShouldBeRejectedForManagerOnly() {
        String token = jwtService.createEmployeeToken(2L, "staff", "STAFF");
        String auth = "Bearer " + token;

        BusinessException ex = assertThrows(BusinessException.class,
                () -> merchantAuthGuard.requireEmployeeRole(auth, "SUPER_ADMIN", "SHOP_MANAGER"));
        org.junit.jupiter.api.Assertions.assertEquals(403, ex.getCode());
    }

    @Test
    void userTokenShouldBeRejectedForMerchantGuard() {
        String token = jwtService.createUserToken(3L, "user");
        String auth = "Bearer " + token;

        assertThrows(UnauthorizedException.class,
                () -> merchantAuthGuard.requireEmployeeRole(auth, "SUPER_ADMIN", "SHOP_MANAGER", "STAFF"));
    }
}
