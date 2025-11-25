package com.app.course.config;

import com.app.common.dto.auth.OAuth2ClientProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OAuth2ClientConfig {

    @Value("${oauth2.client.auth-service-url:http://localhost:8080/auth}")
    private String authServiceUrl;

    @Value("${oauth2.client.token-endpoint:/oauth/token}")
    private String tokenEndpoint;

    @Value("${oauth2.client.client-id:blog-service}")
    private String clientId;

    @Value("${oauth2.client.client-secret:blog-service-secret}")
    private String clientSecret;

    @Value("${oauth2.client.scope:user:read}")
    private String scope;

    @Bean
    public OAuth2ClientProperties oauth2ClientProperties() {
        OAuth2ClientProperties properties = new OAuth2ClientProperties();
        properties.setAuthServiceUrl(authServiceUrl);
        properties.setTokenEndpoint(tokenEndpoint);
        properties.setClientId(clientId);
        properties.setClientSecret(clientSecret);
        properties.setScope(scope);
        return properties;
    }
}