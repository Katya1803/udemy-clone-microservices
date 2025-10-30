package com.app.auth.service;

public interface TokenBlacklistService {

    void blacklistToken(String token);

    boolean isBlacklisted(String token);

    void removeFromBlacklist(String jti);
}
