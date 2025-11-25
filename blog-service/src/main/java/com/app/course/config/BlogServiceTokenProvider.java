package com.app.course.config;

import com.app.common.client.BaseOAuth2TokenProvider;
import com.app.common.dto.auth.OAuth2ClientProperties;
import com.app.common.security.JwtTokenValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class BlogServiceTokenProvider extends BaseOAuth2TokenProvider {

    public BlogServiceTokenProvider(
            OAuth2ClientProperties properties,
            JwtTokenValidator jwtTokenValidator,
            RestTemplate restTemplate) {
        super(properties, jwtTokenValidator, restTemplate);
        log.debug("BlogServiceTokenProvider initialized with clientId: {}", properties.getClientId());
    }
}