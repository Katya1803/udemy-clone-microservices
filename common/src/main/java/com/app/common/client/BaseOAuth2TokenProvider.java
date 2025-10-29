package com.app.common.client;

import com.app.common.dto.auth.CachedToken;
import com.app.common.dto.auth.OAuth2ClientProperties;
import com.app.common.dto.auth.OAuth2TokenResponse;
import com.app.common.security.JwtTokenValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseOAuth2TokenProvider implements IBaseOAuth2TokenProvider {

    private final OAuth2ClientProperties properties;
    private final JwtTokenValidator jwtTokenValidator;
    private final RestTemplate restTemplate;

    private final Map<String, CachedToken> tokenCache = new ConcurrentHashMap<>();
    private static final long EXPIRATION_BUFFER_SECONDS = 30;

    @Override
    public String getServiceToken(String audience) {

        CachedToken cachedToken = tokenCache.get(audience);

        if (cachedToken != null && !isTokenExpiringSoon(cachedToken.getToken())) {
            log.debug("Using cached service token for audience: {}", audience);
            return cachedToken.getToken();
        }

        log.debug("Requesting new service token for audience: {}", audience);
        String newToken = requestServiceToken(audience);

        tokenCache.put(audience, new CachedToken(newToken));

        return newToken;
    }

    @Override
    public void clearCache(String audience) {
        tokenCache.remove(audience);
        log.debug("Cleared cached token for audience: {}", audience);
    }

    private String requestServiceToken(String audience) {
        try {
            // Prepare request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");
            body.add("client_id", properties.getClientId());
            body.add("client_secret", properties.getClientSecret());
            body.add("scope", properties.getScope());
            body.add("audience", audience);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            // Call Auth Service
            ResponseEntity<OAuth2TokenResponse> response = restTemplate.postForEntity(
                    properties.getTokenUrl(),
                    request,
                    OAuth2TokenResponse.class
            );
            log.info("Token endpoint response body: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String token = response.getBody().getAccessToken();
                log.info("Successfully obtained service token for audience: {}", audience);
                return token;
            }

            throw new RuntimeException("Failed to obtain service token: " + response.getStatusCode());

        } catch (Exception e) {
            log.error("Error requesting service token for audience: {}", audience, e);
            throw new RuntimeException("Failed to obtain service token", e);
        }
    }

    private boolean isTokenExpiringSoon(String token) {
        try {
            long remainingSeconds = jwtTokenValidator.getRemainingTime(token);
            return remainingSeconds <= EXPIRATION_BUFFER_SECONDS;
        } catch (Exception e) {
            log.warn("Failed to check token expiration, treating as expired", e);
            return true;
        }
    }
}
