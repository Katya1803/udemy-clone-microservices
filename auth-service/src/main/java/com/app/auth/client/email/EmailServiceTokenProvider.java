package com.app.auth.client.email;

import com.app.common.client.BaseOAuth2TokenProvider;
import com.app.common.dto.auth.OAuth2ClientProperties;
import com.app.common.security.JwtTokenValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Email Service Token Provider
 * Provides service tokens for calling Email Service
 */
@Slf4j
@Component
public class EmailServiceTokenProvider extends BaseOAuth2TokenProvider {

    public EmailServiceTokenProvider(
            OAuth2ClientProperties properties,
            JwtTokenValidator jwtTokenValidator,
            RestTemplate restTemplate) {
        super(properties, jwtTokenValidator, restTemplate);
        log.debug("EmailServiceTokenProvider initialized with clientId: {}", properties.getClientId());
    }
}