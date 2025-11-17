package com.app.auth.client.user;

import com.app.common.client.BaseOAuth2TokenProvider;
import com.app.common.dto.auth.OAuth2ClientProperties;
import com.app.common.security.JwtTokenValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Slf4j
@Component
public class UserServiceTokenProvider extends BaseOAuth2TokenProvider {

    public UserServiceTokenProvider(
            OAuth2ClientProperties properties,
            JwtTokenValidator jwtTokenValidator,
            RestTemplate restTemplate) {
        super(properties, jwtTokenValidator, restTemplate);
        log.debug("UserServiceTokenProvider initialized with clientId: {}", properties.getClientId());
    }
}