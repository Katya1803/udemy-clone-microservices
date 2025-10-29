package com.app.auth.service;

import com.app.auth.dto.LoginRequest;
import com.app.auth.dto.LoginResponse;
import com.app.auth.dto.RegisterRequest;
import com.app.auth.dto.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest registerRequest);

    LoginResponse login(LoginRequest loginRequest);

}
