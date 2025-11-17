package com.app.common.dto.auth;

import com.app.common.constant.SecurityConstants;
import lombok.Data;


@Data
public class OAuth2ClientProperties {

    private String authServiceUrl;

    private String tokenEndpoint = SecurityConstants.TOKEN_ENDPOINT;

    private String clientId;

    private String clientSecret;

    private String scope;

    public String getTokenUrl() {
        return authServiceUrl + tokenEndpoint;
    }
}