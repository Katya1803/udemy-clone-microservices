package com.app.common.security;

import com.app.common.exception.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Token Validator
 * Validates JWT tokens using either:
 * - RSA Public Key (recommended for production)
 * - HMAC Shared Secret (deprecated, for backward compatibility)
 *
 * This class is used by ALL services (auth, user, email, course) and API Gateway
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenValidator {

    private final JwtProperties jwtProperties;

    /**
     * Validate JWT token signature and expiration
     * @return true if valid, throws exception if invalid
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token); // Will throw exception if invalid
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
            throw new InvalidTokenException("Invalid token signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
            throw new InvalidTokenException("Malformed token");
        } catch (ExpiredJwtException ex) {
            log.error("JWT token is expired: {}", ex.getMessage());
            throw new InvalidTokenException("Token expired");
        } catch (UnsupportedJwtException ex) {
            log.error("JWT token is unsupported: {}", ex.getMessage());
            throw new InvalidTokenException("Unsupported token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
            throw new InvalidTokenException("Token claims are empty");
        }
    }

    /**
     * Extract all claims from JWT token
     * Automatically selects RSA or HMAC verification based on configuration
     */
    public Claims getClaims(String token) {
        try {
            if (jwtProperties.isRsaMode()) {
                log.debug("Validating token with RSA public key");
                return Jwts.parser()
                        .verifyWith(getPublicKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
            } else if (jwtProperties.isHmacMode()) {
                log.debug("Validating token with HMAC (consider migrating to RSA for production)");
                return Jwts.parser()
                        .verifyWith(getHmacKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
            } else {
                throw new IllegalStateException("No JWT verification key configured! Set either jwt.publicKey (RSA) or jwt.secret (HMAC)");
            }
        } catch (Exception e) {
            log.error("Failed to extract claims from token: {}", e.getMessage());
            throw new InvalidTokenException("Failed to extract token claims");
        }
    }

    // ========== Token Data Extraction Methods ==========

    public String getUserId(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Get roles as List<String>
     * Parses comma-separated roles: "ROLE_USER,ROLE_ADMIN" -> ["ROLE_USER", "ROLE_ADMIN"]
     */
    public List<String> getRoles(String token) {
        Claims claims = getClaims(token);
        String rolesString = claims.get("roles", String.class);

        if (rolesString == null || rolesString.isBlank()) {
            log.warn("⚠️ No roles in token, using default ROLE_SERVICE");
            return List.of("ROLE_SERVICE");
        }

        // Parse "ROLE_USER,ROLE_ADMIN" → ["ROLE_USER", "ROLE_ADMIN"]
        return Arrays.stream(rolesString.split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Get roles as single comma-separated string
     * For backward compatibility
     */
    public String getRolesAsString(String token) {
        Claims claims = getClaims(token);
        return claims.get("roles", String.class);
    }

    public String getTokenType(String token) {
        return getClaims(token).get("token_type", String.class);
    }

    public String getJti(String token) {
        return getClaims(token).getId();
    }

    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    public Date getExpirationDate(String token) {
        return getClaims(token).getExpiration();
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDate(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public long getRemainingTime(String token) {
        Date expiration = getExpirationDate(token);
        long now = System.currentTimeMillis();
        long expirationTime = expiration.getTime();
        return Math.max(0, (expirationTime - now) / 1000);
    }

    public String getClientId(String token) {
        return getClaims(token).get("client_id", String.class);
    }

    public String getScope(String token) {
        return getClaims(token).get("scope", String.class);
    }

    public String getAudience(String token) {
        return getClaims(token).getAudience().iterator().next();
    }

    // ========== Key Management (Private Methods) ==========

    /**
     * Get RSA Public Key for token verification
     * Public key is safe to share across all services
     */
    private PublicKey getPublicKey() {
        try {
            String publicKeyPEM = jwtProperties.getPublicKey()
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA public key", e);
        }
    }

    /**
     * Get HMAC Secret Key for token verification
     * @deprecated Use RSA instead for production
     */
    @Deprecated
    private SecretKey getHmacKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}