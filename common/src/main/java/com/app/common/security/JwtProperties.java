package com.app.common.security;

import com.app.common.constant.SecurityConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT Configuration Properties
 * Supports both HMAC (symmetric) and RSA (asymmetric) signing
 *
 * PRODUCTION: Use RSA (publicKey/privateKey)
 * DEVELOPMENT: Can use HMAC (secret) for simplicity
 */
@Data
@Component
@ConfigurationProperties(prefix = SecurityConstants.JWT)
public class JwtProperties {

    /**
     * @deprecated Use RSA keys instead (publicKey/privateKey)
     * HMAC shared secret - NOT recommended for production
     */
    @Deprecated
    private String secret;

    /**
     * RSA Public Key (Base64 encoded)
     * Used by ALL services to VERIFY tokens
     * Safe to share across services
     */
    private String publicKey;

    /**
     * RSA Private Key (Base64 encoded)
     * Used ONLY by auth-service to SIGN tokens
     * MUST be kept secret!
     */
    private String privateKey;

    /**
     * JWT Issuer (who created the token)
     */
    private String issuer = SecurityConstants.ISSUER;

    /**
     * Access token expiration in milliseconds
     * Default: 15 minutes (900000ms)
     */
    private Long accessTokenExpiration = SecurityConstants.ACCESS_TOKEN_EXP;

    /**
     * Refresh token expiration in milliseconds
     * Default: 7 days (604800000ms)
     */
    private Long refreshTokenExpiration = SecurityConstants.REFRESH_TOKEN_EXP;

    /**
     * Service-to-service token expiration in milliseconds
     * Default: 5 minutes (300000ms)
     */
    private Long serviceTokenExpiration = SecurityConstants.SERVICE_TOKEN_EXP;

    /**
     * Check if RSA mode is enabled
     * RSA mode requires publicKey (all services) and optionally privateKey (auth-service only)
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