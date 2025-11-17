package com.app.gateway.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;


@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayTokenValidator {

    private final GatewayJwtProperties jwtProperties;


    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String message) {
            super(message);
        }
        public InvalidTokenException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public void validateToken(String token) {
        try {
            getClaims(token);
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
            if (jwtProperties.isRsaMode()) {
                log.debug("Validating token with RSA public key");
                return Jwts.parser()
                        .verifyWith(getPublicKey())
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


    public String getUserId(String token) {
        return getClaims(token).getSubject();
    }

    public String getRole(String token) {
        return getClaims(token).get("roles", String.class);
    }

    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    public String getTokenType(String token) {
        return getClaims(token).get("token_type", String.class);
    }

    public String getJti(String token) {
        return getClaims(token).getId();
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

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

}