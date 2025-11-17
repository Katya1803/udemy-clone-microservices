package com.app.auth.service.impl;

import com.app.auth.entity.Account;
import com.app.auth.service.JwtTokenGenerator;
import com.app.common.constant.SecurityConstants;
import com.app.common.security.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenGeneratorImpl implements JwtTokenGenerator {

    private final JwtProperties jwtProperties;

    @Override
    public String generateAccessToken(Account account) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(jwtProperties.getAccessTokenExpiration());

        String rolesString = account.getRolesAsString();

        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", SecurityConstants.TOKEN_TYPE_USER);
        claims.put("roles", rolesString);
        claims.put("email", account.getEmail());
        claims.put("username", account.getUsername());

        String token = buildToken(claims, account.getId(), now, expiration);

        log.debug("Generated access token for user: {}, roles: {}, expires at: {}",
                account.getUsername(), rolesString, expiration);

        return token;
    }

    @Override
    public String generateServiceToken(String clientId, String audience, String scope) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(jwtProperties.getServiceTokenExpiration());

        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", SecurityConstants.TOKEN_TYPE_SERVICE);
        claims.put("client_id", clientId);
        claims.put("scope", scope);
        claims.put("roles", SecurityConstants.ROLE_SERVICE);

        String token = buildTokenWithAudience(claims, clientId, audience, now, expiration);

        log.debug("Generated service token for client: {}, audience: {}, expires at: {}",
                clientId, audience, expiration);

        return token;
    }

    @Override
    public long getAccessTokenExpirationSeconds() {
        return jwtProperties.getAccessTokenExpiration() / 1000;
    }

    @Override
    public long getServiceTokenExpirationSeconds() {
        return jwtProperties.getServiceTokenExpiration() / 1000;
    }


    private String buildToken(Map<String, Object> claims, String subject, Instant issuedAt, Instant expiration) {
        if (jwtProperties.isRsaMode()) {
            log.debug("Generating token with RSA private key (RS256)");
            return Jwts.builder()
                    .claims(claims)
                    .subject(subject)
                    .issuer(jwtProperties.getIssuer())
                    .issuedAt(Date.from(issuedAt))
                    .expiration(Date.from(expiration))
                    .id(UUID.randomUUID().toString())
                    .signWith(getPrivateKey(), Jwts.SIG.RS256)
                    .compact();
        } else {
            throw new IllegalStateException("No JWT signing key configured! Set either jwt.privateKey (RSA) or jwt.secret (HMAC)");
        }
    }

    /**
     * Build JWT token with audience claim (for service tokens)
     */
    private String buildTokenWithAudience(Map<String, Object> claims, String subject, String audience,
                                          Instant issuedAt, Instant expiration) {
        if (jwtProperties.isRsaMode()) {
            log.debug("Generating service token with RSA private key (RS256)");
            return Jwts.builder()
                    .claims(claims)
                    .subject(subject)
                    .issuer(jwtProperties.getIssuer())
                    .audience().add(audience).and()
                    .issuedAt(Date.from(issuedAt))
                    .expiration(Date.from(expiration))
                    .id(UUID.randomUUID().toString())
                    .signWith(getPrivateKey(), Jwts.SIG.RS256)
                    .compact();
        } else {
            throw new IllegalStateException("No JWT signing key configured! Set either jwt.privateKey (RSA) or jwt.secret (HMAC)");
        }
    }


    private PrivateKey getPrivateKey() {
        try {
            if (jwtProperties.getPrivateKey() == null || jwtProperties.getPrivateKey().isBlank()) {
                throw new IllegalStateException("jwt.privateKey not configured for auth-service!");
            }

            String privateKeyPEM = jwtProperties.getPrivateKey()
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA private key", e);
        }
    }

}