package com.example.springbootblank.auth.security;

import com.example.springbootblank.auth.config.JwtProperties;
import com.example.springbootblank.common.error.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserAuthGuardTest {

    private JwtService jwtService;
    private UserAuthGuard userAuthGuard;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("unit-test-secret-key-should-be-long-enough-123456");
        props.setExpirationMs(86_400_000L);
        jwtService = new JwtService(props);
        userAuthGuard = new UserAuthGuard(jwtService);
    }

    @Test
    void userTokenShouldPass() {
        String token = jwtService.createUserToken(1L, "u1");
        String auth = "Bearer " + token;

        assertDoesNotThrow(() -> userAuthGuard.requireUser(auth));
    }

    @Test
    void employeeTokenShouldBeRejectedForUserGuard() {
        String token = jwtService.createEmployeeToken(2L, "admin", "SUPER_ADMIN");
        String auth = "Bearer " + token;

        BusinessException ex = assertThrows(BusinessException.class, () -> userAuthGuard.requireUser(auth));
        assertEquals(403, ex.getCode());
    }

    @Test
    void riderTokenShouldBeRejectedForUserGuard() {
        String token = jwtService.createRiderToken(3L, "r1");
        String auth = "Bearer " + token;

        BusinessException ex = assertThrows(BusinessException.class, () -> userAuthGuard.requireUser(auth));
        assertEquals(403, ex.getCode());
    }
}
