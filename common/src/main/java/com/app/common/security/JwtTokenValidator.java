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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenValidator {

    private final JwtProperties jwtProperties;

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

    // ✅ FIXED: Return List<String> by parsing comma-separated string
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

    // ✅ NEW: Get roles as single string (for backward compatibility)
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

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}