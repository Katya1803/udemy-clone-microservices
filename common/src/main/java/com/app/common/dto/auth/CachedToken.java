package com.app.common.dto.auth;

import lombok.Data;

@Data
public class CachedToken {
    private final String token;
    private final long cachedAt = System.currentTimeMillis();
}
