package com.app.auth.service.impl;

import com.app.auth.client.email.EmailServiceClient;
import com.app.auth.constant.AccountStatus;
import com.app.auth.constant.Role;
import com.app.auth.dto.*;
import com.app.auth.entity.Account;
import com.app.auth.repository.AccountRepository;
import com.app.auth.service.*;
import com.app.common.constant.ErrorCode;
import com.app.common.dto.common.EmailRequest;
import com.app.common.exception.InvalidTokenException;
import com.app.common.exception.UnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final EmailServiceClient emailServiceClient;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest registerRequest) {
        log.info("Register attempt for username: {}", registerRequest.getUsername());

        if (accountRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("Username is already in use");
        }

        if (accountRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        Account account = Account.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .roles(Role.USER)
                .status(AccountStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .build();

        account = accountRepository.save(account);

        String otp = otpService.generateOtp(account.getEmail());

        try {
            sendOtpEmail(account.getEmail(), account.getUsername(), otp);
            log.info("OTP sent to email: {}", account.getEmail());
        } catch (Exception e) {
            log.error("Failed to send OTP to email: {}", account.getEmail(), e);
        }

        log.info("Register successful for username: {}", account.getUsername());
        return RegisterResponse.builder()
                .id(account.getId())
                .username(account.getUsername())
                .email(account.getEmail())
                .needsVerification(true)
                .message("Registration successful. Please check your email for OTP verification code.")
                .build();
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {

        Account account = accountRepository.findByUsernameOrEmail(loginRequest.getAccount())
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS.getMessage()));

        if (!passwordEncoder.matches(loginRequest.getPassword(), account.getPassword())) {
            log.warn("Invalid password for user: {}", loginRequest.getAccount());
            throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS.getMessage());
        }

        if (account.isPendingVerification()) {
            log.warn("User account is pending verification: {}", loginRequest.getAccount());
            throw new UnauthorizedException("Please verify your email before logging in");
        }

        if (!account.isActive()) {
            log.warn("User account is not active: {}", loginRequest.getAccount());
            throw new UnauthorizedException("User account is not active");
        }

        String accessToken = jwtTokenGenerator.generateAccessToken(account);
        String refreshToken = refreshTokenService.createRefreshToken(account, loginRequest.getDeviceId());

        log.info("Login successful for user: {}", loginRequest.getAccount());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(1L)
                .user(LoginResponse.UserInfo.builder()
                        .id(account.getId())
                        .username(account.getUsername())
                        .email(account.getEmail())
                        .roles(account.getRoles().toString())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public LoginResponse verifyOtp(VerifyOtpRequest request) {
        log.info("OTP verification attempt for email: {}", request.getEmail());

        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (account.isActive()) {
            log.warn("User already verified: {}", request.getEmail());
            throw new IllegalStateException("Account already verified");
        }

        boolean isValid = otpService.validateOtp(request.getEmail(), request.getOtp());
        if (!isValid) {
            log.warn("Invalid OTP for email: {}", request.getEmail());
            throw new UnauthorizedException("Invalid or expired OTP");
        }

        account.activate();
        accountRepository.save(account);
        log.info("User activated successfully: {}", account.getUsername());


        String accessToken = jwtTokenGenerator.generateAccessToken(account);
        String refreshToken = refreshTokenService.createRefreshToken(
                account,
                request.getDeviceId() != null ? request.getDeviceId() : "web"
        );

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenGenerator.getAccessTokenExpirationSeconds())
                .user(LoginResponse.UserInfo.builder()
                        .id(account.getId())
                        .username(account.getUsername())
                        .email(account.getEmail())
                        .roles(account.getRoles().toString())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public void resendOtp(ResendOtpRequest request) {
        log.info("Resend OTP request for email: {}", request.getEmail());

        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (account.isActive()) {
            throw new IllegalStateException("Account already verified");
        }

        if (!otpService.canRequestOtp(request.getEmail())) {
            throw new IllegalStateException("Too many OTP requests. Please try again later.");
        }

        String otp = otpService.generateOtp(account.getEmail());
        otpService.incrementOtpRequest(account.getEmail());

        try {
            sendOtpEmail(account.getEmail(), account.getUsername(), otp);
            log.info("OTP resent to email: {}", account.getEmail());
        } catch (Exception e) {
            log.error("Failed to resend OTP email: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send OTP email");
        }
    }

    @Override
    @Transactional
    public LoginResponse refresh(RefreshTokenRequest request) {
        log.debug("Token refresh attempt");

        String userId = refreshTokenService.verifyRefreshToken(request.getRefreshToken());

        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("User not found"));

        if (!account.isActive()) {
            refreshTokenService.revokeRefreshToken(request.getRefreshToken());
            throw new UnauthorizedException("User account is not active");
        }

        String newAccessToken = jwtTokenGenerator.generateAccessToken(account);

        refreshTokenService.revokeRefreshToken(request.getRefreshToken());
        String newRefreshToken = refreshTokenService.createRefreshToken(account, null);

        log.info("Token refresh successful for user: {}", account.getUsername());

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenGenerator.getAccessTokenExpirationSeconds())
                .build();
    }

    @Override
    public void logout(String accessToken, String accountId) {
        log.info("Logout for user: {}", accountId);

        tokenBlacklistService.blacklistToken(accessToken);
        refreshTokenService.revokeAllUserTokens(accountId);

        log.info("Logout successful for user: {}", accountId);
    }

    private void sendOtpEmail(String email, String username, String otp) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("username", username);
        variables.put("otp", otp);

        EmailRequest emailRequest = EmailRequest.builder()
                .to(email)
                .subject("Email Verification - Your OTP Code")
                .template("otp-email")
                .variables(variables)
                .build();

        emailServiceClient.sendEmail(emailRequest);
    }
}
