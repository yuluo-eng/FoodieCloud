package com.example.springbootblank.auth.service;

import com.example.springbootblank.auth.dto.AuthMeResponse;
import com.example.springbootblank.auth.dto.LoginRequest;
import com.example.springbootblank.auth.dto.UserRegisterRequest;

import java.util.Map;

public interface AuthService {

    Map<String, Object> register(UserRegisterRequest req);

    Map<String, Object> userLogin(LoginRequest req);

    Map<String, Object> employeeLogin(LoginRequest req);

    AuthMeResponse me(String authorizationHeader);
}
