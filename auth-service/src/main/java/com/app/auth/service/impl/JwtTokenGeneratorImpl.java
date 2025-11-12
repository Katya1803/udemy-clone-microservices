package com.app.auth.service.impl;

import com.app.auth.client.user.UserServiceClient;
import com.app.auth.entity.Account;
import com.app.auth.service.JwtTokenGenerator;
import com.app.common.constant.SecurityConstants;
import com.app.common.security.JwtProperties;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

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

        String token = Jwts.builder()
                .subject(account.getId())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .id(UUID.randomUUID().toString())
                .claims(claims)
                .signWith(getSigningKey())
                .compact();

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
        // ✅ FIXED: Service tokens have ROLE_SERVICE
        claims.put("roles", SecurityConstants.ROLE_SERVICE); // Single role as String

        String token = Jwts.builder()
                .subject(clientId)
                .issuer(jwtProperties.getIssuer())
                .audience().add(audience).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .id(UUID.randomUUID().toString())
                .claims(claims)
                .signWith(getSigningKey())
                .compact();

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

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}