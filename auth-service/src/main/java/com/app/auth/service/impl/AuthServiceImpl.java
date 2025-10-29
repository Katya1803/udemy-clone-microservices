package com.app.auth.service.impl;

import com.app.auth.constant.AccountStatus;
import com.app.auth.constant.Role;
import com.app.auth.dto.LoginRequest;
import com.app.auth.dto.LoginResponse;
import com.app.auth.dto.RegisterRequest;
import com.app.auth.dto.RegisterResponse;
import com.app.auth.entity.Account;
import com.app.auth.repository.AccountRepository;
import com.app.auth.service.AuthService;
import com.app.common.constant.ErrorCode;
import com.app.common.exception.UnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;

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
                .password(registerRequest.getPassword())
                .roles(Role.USER)
                .status(AccountStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .build();

        account = accountRepository.save(account);

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

        if (account.isPendingVerification()) {
            log.warn("User account is pending verification: {}", loginRequest.getAccount());
            throw new UnauthorizedException("Please verify your email before logging in");
        }

        if (!account.isActive()) {
            log.warn("User account is not active: {}", loginRequest.getAccount());
            throw new UnauthorizedException("User account is not active");
        }

        String accessToken = UUID.randomUUID().toString();
        String refreshToken = UUID.randomUUID().toString();

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
}
