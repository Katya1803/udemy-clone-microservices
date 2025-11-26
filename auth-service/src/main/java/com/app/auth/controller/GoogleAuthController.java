package com.app.auth.controller;

import com.app.auth.dto.GoogleLoginRequest;
import com.app.auth.dto.LoginResponse;
import com.app.auth.service.GoogleOAuthService;
import com.app.common.constant.SecurityConstants;
import com.app.common.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth/google")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final GoogleOAuthService googleOAuthService;

    @PostMapping("/callback")
    public ResponseEntity<ApiResponse<LoginResponse>> googleCallback(
            @Valid @RequestBody GoogleLoginRequest request) {

        log.info("Google OAuth callback received");

        LoginResponse response = googleOAuthService.loginWithGoogle(request);

        ResponseCookie refreshCookie = ResponseCookie.from("refresh", response.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(SecurityConstants.REFRESH_TOKEN_EXP)
                .build();

        LoginResponse body = new LoginResponse(
                response.getAccessToken(),
                null,
                response.getTokenType(),
                response.getExpiresIn(),
                response.getUser()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.success(body, "Google login successful"));
    }
}