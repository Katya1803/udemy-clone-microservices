package com.app.gateway.service;

import com.app.gateway.security.GatewayTokenValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GatewayTokenBlacklist {
    private static final String BLACKLIST_KEY_PREFIX = "blacklist:";

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final GatewayTokenValidator jwtTokenValidator;

    public Mono<Boolean> isBlacklisted(String token) {
        try {
            String jti = jwtTokenValidator.getJti(token);
            String key = BLACKLIST_KEY_PREFIX + jti;

            return reactiveRedisTemplate.hasKey(key)
                    .doOnSuccess(exists -> {
                        if (Boolean.TRUE.equals(exists)) {
                            log.debug("Token is blacklisted: {}", jti);
                        }
                    })
                    .onErrorResume(error -> {
                        log.error("Error checking blacklist: {}", error.getMessage());
                        return Mono.just(false);
                    });

        } catch (Exception e) {
            log.error("Error extracting JTI from token: {}", e.getMessage());
            return Mono.just(false);
        }
    }
}
