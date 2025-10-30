package com.app.auth.controller;

import com.app.auth.dto.*;
import com.app.auth.service.AuthService;
import com.app.common.constant.SecurityConstants;
import com.app.common.dto.response.ApiResponse;
import com.app.common.util.CurrentAccount;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Registration request for username: {}", request.getUsername());

        RegisterResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, response.getMessage()));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login request for username: {}", request.getAccount());

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Login successful")
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        log.info("OTP verification request for email: {}", request.getEmail());

        LoginResponse response = authService.verifyOtp(request);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Email verified successfully. You are now logged in.")
        );
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(
            @Valid @RequestBody ResendOtpRequest request) {

        log.info("Resend OTP request for email: {}", request.getEmail());

        authService.resendOtp(request);

        return ResponseEntity.ok(
                ApiResponse.success("OTP sent successfully. Please check your email.")
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        log.debug("Token refresh request");

        LoginResponse response = authService.refresh(request);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Token refreshed successfully")
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(SecurityConstants.JWT_HEADER) String authHeader,
            @CurrentAccount String accountId) {

        log.info("Logout request for account: {}", accountId);

        String token = authHeader.replace(SecurityConstants.JWT_PREFIX, "");

        authService.logout(token, accountId);

        return ResponseEntity.ok(
                ApiResponse.success("Logout successful")
        );
    }
}
