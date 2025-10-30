package com.app.auth.service;

import com.app.auth.dto.OAuth2TokenRequest;
import com.app.common.dto.auth.OAuth2TokenResponse;

/**
 * Handles service-to-service authentication (Client Credentials flow)
 */
public interface OAuth2ClientService {

    OAuth2TokenResponse generateServiceToken(OAuth2TokenRequest request);
}
