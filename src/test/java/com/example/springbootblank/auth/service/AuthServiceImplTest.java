package com.example.springbootblank.auth.service;

import com.example.springbootblank.auth.dto.LoginRequest;
import com.example.springbootblank.auth.dto.RiderRegisterRequest;
import com.example.springbootblank.auth.mapper.AuthMapper;
import com.example.springbootblank.auth.security.JwtService;
import com.example.springbootblank.common.error.BusinessException;
import com.example.springbootblank.common.error.UnauthorizedException;
import com.example.springbootblank.rider.entity.Rider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthMapper authMapper;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void riderRegisterShouldInsertRiderWhenUsernameAndPhoneAvailable() {
        RiderRegisterRequest req = new RiderRegisterRequest("rider02", "123456", "李骑手", "13900000010");
        when(authMapper.countRiderByUsername("rider02")).thenReturn(0);
        when(authMapper.countRiderByPhone("13900000010")).thenReturn(0);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        doAnswer(invocation -> {
            Rider rider = invocation.getArgument(0);
            rider.setId(2L);
            return 1;
        }).when(authMapper).insertRider(any(Rider.class));

        Map<String, Object> result = authService.riderRegister(req);

        assertEquals(2L, result.get("riderId"));
        ArgumentCaptor<Rider> riderCaptor = ArgumentCaptor.forClass(Rider.class);
        verify(authMapper).insertRider(riderCaptor.capture());
        Rider inserted = riderCaptor.getValue();
        assertEquals("rider02", inserted.getUsername());
        assertEquals("李骑手", inserted.getRealName());
        assertEquals("13900000010", inserted.getPhone());
        assertEquals(1, inserted.getEnabled());
        assertEquals("ONLINE", inserted.getWorkStatus());
        assertEquals("encoded-password", inserted.getPassword());
    }

    @Test
    void riderRegisterShouldThrowWhenUsernameExists() {
        RiderRegisterRequest req = new RiderRegisterRequest("rider02", "123456", "李骑手", "13900000010");
        when(authMapper.countRiderByUsername("rider02")).thenReturn(1);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.riderRegister(req));

        assertEquals(400, ex.getCode());
        assertEquals("用户名已存在", ex.getMessage());
    }

    @Test
    void riderRegisterShouldThrowWhenPhoneExists() {
        RiderRegisterRequest req = new RiderRegisterRequest("rider02", "123456", "李骑手", "13900000010");
        when(authMapper.countRiderByUsername("rider02")).thenReturn(0);
        when(authMapper.countRiderByPhone("13900000010")).thenReturn(1);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.riderRegister(req));

        assertEquals(400, ex.getCode());
        assertEquals("手机号已被注册", ex.getMessage());
    }

    @Test
    void riderLoginShouldReturnTokenAndRiderInfoWhenCredentialsCorrect() {
        Rider rider = new Rider();
        rider.setId(9L);
        rider.setUsername("rider09");
        rider.setPassword("encoded");
        rider.setRealName("王骑手");
        rider.setEnabled(1);
        rider.setWorkStatus("ONLINE");
        when(authMapper.findRiderByUsername("rider09")).thenReturn(rider);
        when(passwordEncoder.matches("123456", "encoded")).thenReturn(true);
        when(jwtService.createRiderToken(9L, "rider09")).thenReturn("rider-token");

        Map<String, Object> result = authService.riderLogin(new LoginRequest("rider09", "123456"));

        assertEquals("rider-token", result.get("token"));
        Map<?, ?> riderInfo = (Map<?, ?>) result.get("riderInfo");
        assertEquals(9L, riderInfo.get("id"));
        assertEquals("rider09", riderInfo.get("username"));
        assertEquals("王骑手", riderInfo.get("realName"));
        assertEquals("ONLINE", riderInfo.get("workStatus"));
    }

    @Test
    void riderLoginShouldThrowWhenPasswordIncorrect() {
        Rider rider = new Rider();
        rider.setId(10L);
        rider.setUsername("rider10");
        rider.setPassword("encoded");
        rider.setEnabled(1);
        when(authMapper.findRiderByUsername("rider10")).thenReturn(rider);
        when(passwordEncoder.matches("bad", "encoded")).thenReturn(false);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> authService.riderLogin(new LoginRequest("rider10", "bad")));

        assertNotEquals("", ex.getMessage());
    }
}
