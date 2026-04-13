package com.example.springbootblank.auth.service;

import com.example.springbootblank.auth.dto.AuthMeResponse;
import com.example.springbootblank.auth.dto.LoginRequest;
import com.example.springbootblank.auth.dto.UserRegisterRequest;
import com.example.springbootblank.auth.entity.User;
import com.example.springbootblank.auth.mapper.AuthMapper;
import com.example.springbootblank.auth.security.JwtService;
import com.example.springbootblank.common.error.BusinessException;
import com.example.springbootblank.common.error.UnauthorizedException;
import com.example.springbootblank.employee.entity.Employee;
import io.jsonwebtoken.JwtException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthMapper authMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(AuthMapper authMapper, BCryptPasswordEncoder passwordEncoder, JwtService jwtService) {
        this.authMapper = authMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public Map<String, Object> register(UserRegisterRequest req) {
        String username = req.username().trim();
        if (authMapper.countUserByUsername(username) > 0) {
            throw new BusinessException(400, "用户名已存在");
        }
        String phone = StringUtils.hasText(req.phone()) ? req.phone().trim() : null;
        if (phone != null && authMapper.countUserByPhone(phone) > 0) {
            throw new BusinessException(400, "手机号已被注册");
        }
        String nickname = StringUtils.hasText(req.nickname()) ? req.nickname().trim() : null;

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setPhone(phone);
        user.setNickname(nickname);
        user.setStatus(1);

        authMapper.insertUser(user);
        return Map.of("userId", user.getId());
    }

    @Override
    public Map<String, Object> userLogin(LoginRequest req) {
        User user = authMapper.findUserByUsername(req.username().trim());
        if (user == null) {
            throw new UnauthorizedException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new UnauthorizedException("账号已禁用");
        }
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new UnauthorizedException("用户名或密码错误");
        }

        String token = jwtService.createUserToken(user.getId(), user.getUsername());
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname() != null ? user.getNickname() : user.getUsername());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userInfo", userInfo);
        return data;
    }

    @Override
    public Map<String, Object> employeeLogin(LoginRequest req) {
        Employee employee = authMapper.findEmployeeByUsernameWithRole(req.username().trim());
        if (employee == null) {
            throw new UnauthorizedException("用户名或密码错误");
        }
        if (employee.getEnabled() != null && employee.getEnabled() == 0) {
            throw new UnauthorizedException("账号已禁用");
        }
        if (!passwordEncoder.matches(req.password(), employee.getPassword())) {
            throw new UnauthorizedException("用户名或密码错误");
        }

        String token = jwtService.createEmployeeToken(
                employee.getId(),
                employee.getUsername(),
                employee.getRoleCode()
        );
        Map<String, Object> employeeInfo = new HashMap<>();
        employeeInfo.put("id", employee.getId());
        employeeInfo.put("username", employee.getUsername());
        employeeInfo.put("realName", employee.getRealName());
        employeeInfo.put("roleCode", employee.getRoleCode());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("employeeInfo", employeeInfo);
        return data;
    }

    @Override
    public AuthMeResponse me(String authorizationHeader) {
        String token = extractBearer(authorizationHeader);
        JwtService.JwtPrincipal principal;
        try {
            principal = jwtService.parse(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("未登录或 Token 无效");
        }

        if (JwtService.TYPE_USER.equals(principal.type())) {
            User user = authMapper.findUserById(principal.id());
            if (user == null || (user.getStatus() != null && user.getStatus() == 0)) {
                throw new UnauthorizedException("未登录或 Token 无效");
            }
            return new AuthMeResponse(user.getId(), JwtService.TYPE_USER, user.getUsername(), null);
        }
        if (JwtService.TYPE_EMPLOYEE.equals(principal.type())) {
            Employee employee = authMapper.findEmployeeByIdWithRole(principal.id());
            if (employee == null || (employee.getEnabled() != null && employee.getEnabled() == 0)) {
                throw new UnauthorizedException("未登录或 Token 无效");
            }
            return new AuthMeResponse(
                    employee.getId(),
                    JwtService.TYPE_EMPLOYEE,
                    employee.getUsername(),
                    employee.getRoleCode()
            );
        }
        throw new UnauthorizedException("未登录或 Token 无效");
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
