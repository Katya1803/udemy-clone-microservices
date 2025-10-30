package com.app.auth.service;

import com.app.auth.entity.Account;

public interface RefreshTokenService {

    String createRefreshToken(Account account, String deviceId);

    String verifyRefreshToken(String rawToken);

    void revokeRefreshToken(String rawToken);

    void revokeAllUserTokens(String accountId);
}
