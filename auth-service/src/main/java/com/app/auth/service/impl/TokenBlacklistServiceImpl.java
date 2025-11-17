package com.app.auth.service.impl;

import com.app.auth.service.TokenBlacklistService;
import com.app.common.constant.RedisConstants;
import com.app.common.security.JwtTokenValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenValidator jwtTokenValidator;

    @Override
    public void blacklistToken(String token) {
        try {
            String jti = jwtTokenValidator.getJti(token);
            long remainingSeconds = jwtTokenValidator.getRemainingTime(token);

            if (remainingSeconds > 0) {
                String key = RedisConstants.REDIS_BLACKLIST_PREFIX + jti;
                redisTemplate.opsForValue().set(key, "1", remainingSeconds, TimeUnit.SECONDS);

                log.info("Blacklisted token JTI: {}, TTL: {}s", jti, remainingSeconds);
            } else {
                log.debug("Token already expired, no need to blacklist: {}", jti);
            }

        } catch (Exception e) {
            log.error("Failed to blacklist token", e);
            throw new RuntimeException("Failed to blacklist token", e);
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        try {
            String jti = jwtTokenValidator.getJti(token);
            String key = RedisConstants.REDIS_BLACKLIST_PREFIX + jti;
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.warn("Failed to check blacklist status for token", e);
            return false;
        }
    }

    @Override
    public void removeFromBlacklist(String jti) {
        String key = RedisConstants.REDIS_BLACKLIST_PREFIX + jti;
        redisTemplate.delete(key);
        log.info("Removed JTI from blacklist: {}", jti);
    }
}
