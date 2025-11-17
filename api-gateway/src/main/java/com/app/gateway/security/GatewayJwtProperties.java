package com.app.gateway.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT Configuration Properties for API Gateway (Reactive)
 * Supports both HMAC (symmetric) and RSA (asymmetric) verification
 *
 * PRODUCTION: Use RSA (publicKey)
 * DEVELOPMENT: Can use HMAC (secret) for simplicity
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class GatewayJwtProperties {

    /**
     * @deprecated Use RSA keys instead (publicKey)
     * HMAC shared secret - NOT recommended for production
     */
    @Deprecated
    private String secret;

    /**
     * RSA Public Key (Base64 encoded)
     * Used by API Gateway to VERIFY tokens
     * Safe to share across services
     */
    private String publicKey;

    /**
     * JWT Issuer (who created the token)
     */
    private String issuer = "auth-service";

    /**
     * Access token expiration in milliseconds
     * Used for validation only (not for generation)
     */
    private Long accessTokenExpiration = 900000L;

    /**
     * Refresh token expiration in milliseconds
     */
    private Long refreshTokenExpiration = 604800000L;

    /**
     * Service-to-service token expiration in milliseconds
     */
    private Long serviceTokenExpiration = 300000L;

    /**
     * Check if RSA mode is enabled
     */
    public boolean isRsaMode() {
        return publicKey != null && !publicKey.isBlank();
    }

    /**
     * Check if HMAC mode is enabled (deprecated)
     */
    public boolean isHmacMode() {
        return !isRsaMode() && secret != null && !secret.isBlank();
    }
}