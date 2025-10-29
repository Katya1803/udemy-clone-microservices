package com.app.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.Instant;

/**
 * Refresh Token Entity (Redis)
 * Stores refresh tokens with TTL
 */
@RedisHash(value = "refresh_token", timeToLive = 60 * 60 * 24 * 7)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String deviceId;

    @Builder.Default
    private Instant createdAt = Instant.now();

    private Instant expiresAt;
}