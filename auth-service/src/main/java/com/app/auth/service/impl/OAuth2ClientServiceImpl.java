package com.app.auth.service.impl;

import com.app.auth.dto.OAuth2TokenRequest;
import com.app.auth.entity.ServiceClient;
import com.app.auth.repository.ServiceClientRepository;
import com.app.auth.service.JwtTokenGenerator;
import com.app.auth.service.OAuth2ClientService;
import com.app.common.constant.ErrorCode;
import com.app.common.constant.SecurityConstants;
import com.app.common.dto.auth.OAuth2TokenResponse;
import com.app.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2ClientServiceImpl implements OAuth2ClientService {

    private final ServiceClientRepository serviceClientRepository;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2TokenResponse generateServiceToken(OAuth2TokenRequest request) {

        if(!SecurityConstants.CLIENT_CREDENTIALS.equals(request.getClientId())) {
            throw new UnauthorizedException("Invalid grant type. Only 'client_credentials' is supported");
        }

        ServiceClient client = serviceClientRepository.findByClientId(request.getClientId())
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.INVALID_CLIENT.getMessage()));

        if (!client.getEnabled()) {
            throw new UnauthorizedException("Service client is disabled");
        }

        if (!passwordEncoder.matches(request.getClientSecret(), client.getClientSecret())) {
            throw new UnauthorizedException("Invalid client credentials");
        }

        String requestedScope = request.getScope() != null ? request.getScope() : client.getAllowedScopes();
        if (!isValidScope(client, requestedScope)) {
            throw new UnauthorizedException("Requested scope is not allowed for this client");
        }

        String audience = request.getAudience() != null ? request.getAudience() : "default";

        String accessToken = jwtTokenGenerator.generateServiceToken(
                client.getClientId(),
                audience,
                requestedScope
        );

        log.info("Generated service token for client: {}, audience: {}",
                client.getClientId(), audience);

        return OAuth2TokenResponse.builder()
                .accessToken(accessToken)
                .tokenType(SecurityConstants.TOKEN_TYPE)
                .expiresIn(jwtTokenGenerator.getServiceTokenExpirationSeconds())
                .scope(requestedScope)
                .build();
    }

    private boolean isValidScope(ServiceClient client, String requestedScope) {
        if (requestedScope == null || requestedScope.isEmpty()) {
            return true;
        }

        String[] requestedScopes = requestedScope.split(",");
        for (String scope : requestedScopes) {
            if (!client.hasScope(scope.trim())) {
                log.warn("Client {} requested invalid scope: {}", client.getClientId(), scope);
                return false;
            }
        }
        return true;
    }
}
