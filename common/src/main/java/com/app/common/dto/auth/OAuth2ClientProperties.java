package com.app.common.dto.auth;

import com.app.common.constant.SecurityConstants;
import lombok.Data;

/**
 * OAuth2 Client Configuration Properties
 * Each service should configure its own credentials
 */
@Data
public class OAuth2ClientProperties {

    /**
     * Auth service URL
     */
    private String authServiceUrl;

    /**
     * OAuth2 token endpoint
     * Example: /oauth/token
     */
    private String tokenEndpoint = SecurityConstants.TOKEN_ENDPOINT;

    /**
     * Client ID (service identifier)
     * Example: "test-service", "user-service"
     */
    private String clientId;

    /**
     * Client secret
     */
    private String clientSecret;

    /**
     * Default scope for service tokens
     * Example: "user:read user:write"
     */
    private String scope;

    /**
     * Get full token URL
     */
    public String getTokenUrl() {
        return authServiceUrl + tokenEndpoint;
    }
}