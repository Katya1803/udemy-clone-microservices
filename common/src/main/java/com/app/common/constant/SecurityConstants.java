package com.app.common.constant;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class SecurityConstants {

    // Token headers
    public static final String TOKEN_TYPE = "Bearer";
    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_PREFIX = "Bearer ";

    // Services to Services
    public static final String HEADER_USER_ROLES = "X-User-Roles";
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String TOKEN_ENDPOINT = "/oauth/token";

    // Token types
    public static final String TOKEN_TYPE_USER = "USER_TOKEN";
    public static final String TOKEN_TYPE_SERVICE = "SERVICE_TOKEN";

    // JWT properties
    public static final String JWT = "jwt";
    public static final String ISSUER = "auth-service";
    public static Long ACCESS_TOKEN_EXP = 900000L;
    public static Long REFRESH_TOKEN_EXP = 604800000L;
    public static Long SERVICE_TOKEN_EXP = 300000L;

    public static String ROLE_SERVICE = "ROLE_SERVICE";
}
