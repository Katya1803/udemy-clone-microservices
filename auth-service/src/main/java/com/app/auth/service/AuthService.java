package com.app.auth.service;

import com.app.auth.dto.*;

public interface AuthService {

    RegisterResponse register(RegisterRequest registerRequest);

    LoginResponse login(LoginRequest loginRequest);

    LoginResponse verifyOtp(VerifyOtpRequest request);

    void resendOtp(ResendOtpRequest request);

    LoginResponse refresh(RefreshTokenRequest request);

    void logout(String accessToken, String accountId);

}
