package com.app.auth.service.impl;

import com.app.auth.entity.Account;
import com.app.auth.entity.RefreshToken;
import com.app.auth.repository.RefreshTokenRepository;
import com.app.auth.service.RefreshTokenService;
import com.app.common.constant.SecurityConstants;
import com.app.common.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public String createRefreshToken(Account account, String deviceId) {
        String rawToken = UUID.randomUUID() + ":" + account.getId();
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .id(tokenHash)
                .accountId(account.getId())
                .deviceId(deviceId)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(SecurityConstants.REFRESH_TOKEN_EXP))
                .build();

        refreshTokenRepository.save(refreshToken);

        log.info("Created refresh token for account: {}, device : {}",
                account.getUsername(), deviceId);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(rawToken.getBytes());
    }

    @Override
    public String verifyRefreshToken(String rawToken) {
        try {
            String decodedToken = new String(Base64.getUrlDecoder().decode(rawToken));
            String tokenHash = hashToken(decodedToken);

            RefreshToken refreshToken = refreshTokenRepository.findById(tokenHash)
                    .orElseThrow(() -> new InvalidTokenException("Refresh token not found or expired"));

            if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
                refreshTokenRepository.deleteById(tokenHash);
                throw new InvalidTokenException("Refresh token expired");
            }

            log.debug("Refresh token verified for user: {}", refreshToken.getAccountId());
            return refreshToken.getAccountId();

        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid refresh token format");
        }
    }

    @Override
    public void revokeRefreshToken(String rawToken) {
        try {
            String decodedToken = new String(Base64.getUrlDecoder().decode(rawToken));
            String tokenHash = hashToken(decodedToken);
            refreshTokenRepository.deleteById(tokenHash);
            log.info("Revoked refresh token: {}", tokenHash);
        } catch (Exception e) {
            log.warn("Failed to revoke refresh token", e);
        }
    }

    @Override
    public void revokeAllUserTokens(String accountId) {
        List<RefreshToken> tokens = refreshTokenRepository.findByAccountId(accountId);
        tokens.forEach(token -> refreshTokenRepository.deleteById(token.getId()));
        log.info("Revoked all refresh tokens for user: {}, count: {}", accountId, tokens.size());
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
