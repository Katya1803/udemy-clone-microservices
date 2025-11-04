package com.app.gateway.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class GatewayTokenValidator {

    private final String secret;
    private final String issuer;

    public GatewayTokenValidator(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.issuer}") String issuer) {
        this.secret = secret;
        this.issuer = issuer;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
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

    public Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Failed to extract claims from token: {}", e.getMessage());
            throw new InvalidTokenException("Failed to extract token claims");
        }
    }

    public String getUserId(String token) {
        return getClaims(token).getSubject();
    }

    // ✅ FIXED: Return comma-separated string directly
    public String getRole(String token) {
        Claims claims = getClaims(token);
        String roles = claims.get("roles", String.class);

        if (roles == null || roles.isBlank()) {
            log.warn("⚠️ No roles in token");
            return "ROLE_SERVICE"; // Default for service tokens
        }

        return roles; // Return "ROLE_USER,ROLE_ADMIN"
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

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String message) {
            super(message);
        }
    }
}