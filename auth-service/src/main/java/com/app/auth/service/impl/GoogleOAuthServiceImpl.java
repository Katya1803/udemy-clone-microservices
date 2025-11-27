package com.app.auth.service.impl;

import com.app.auth.client.user.ResilientUserServiceClient;
import com.app.auth.constant.AccountStatus;
import com.app.auth.constant.Provider;
import com.app.auth.constant.Role;
import com.app.auth.dto.GoogleLoginRequest;
import com.app.auth.dto.GoogleUserInfo;
import com.app.auth.dto.LoginResponse;
import com.app.auth.entity.Account;
import com.app.auth.entity.AuthIdentity;
import com.app.auth.repository.AccountRepository;
import com.app.auth.repository.AuthIdentityRepository;
import com.app.auth.service.GoogleOAuthService;
import com.app.auth.service.JwtTokenGenerator;
import com.app.auth.service.RefreshTokenService;
import com.app.auth.service.event.AccountEventPublisher;
import com.app.common.dto.common.CreateUserRequest;
import com.app.common.exception.UnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOAuthServiceImpl implements GoogleOAuthService {

    private final AccountRepository accountRepository;
    private final AuthIdentityRepository authIdentityRepository;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final RefreshTokenService refreshTokenService;
    private final ResilientUserServiceClient userServiceClient;
    private final AccountEventPublisher accountEventPublisher;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${oauth2.google.client-id}")
    private String clientId;

    @Value("${oauth2.google.client-secret}")
    private String clientSecret;

    @Value("${oauth2.google.redirect-uri:http://localhost:3000/auth/google/callback}")
    private String redirectUri;

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    @Override
    public GoogleUserInfo getUserInfo(String code) {
        String accessToken = exchangeCodeForToken(code);
        return fetchUserInfo(accessToken);
    }

    @Override
    @Transactional
    public LoginResponse loginWithGoogle(GoogleLoginRequest request) {
        log.info("Google login attempt with code");

        GoogleUserInfo googleUser = getUserInfo(request.getCode());

        if (googleUser.getEmail() == null || googleUser.getId() == null) {
            throw new UnauthorizedException("Failed to get user info from Google");
        }

        log.info("Google user info retrieved: {}", googleUser.getEmail());

        AuthIdentity authIdentity = authIdentityRepository
                .findByProviderAndProviderId(Provider.GOOGLE, googleUser.getId())
                .orElse(null);

        Account account;

        if (authIdentity != null) {
            account = authIdentity.getAccount();
            log.info("Existing Google account found: {}", account.getUsername());
        } else {
            account = createOrLinkAccount(googleUser);
        }

        if (!account.isActive()) {
            log.warn("Google account is not active: {}", account.getUsername());
            throw new UnauthorizedException("Account is not active");
        }

        String accessToken = jwtTokenGenerator.generateAccessToken(account);
        String refreshToken = refreshTokenService.createRefreshToken(account, request.getDeviceId());

        log.info("Google login successful for user: {}", account.getUsername());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenGenerator.getAccessTokenExpirationSeconds())
                .user(LoginResponse.UserInfo.builder()
                        .id(account.getId())
                        .username(account.getUsername())
                        .email(account.getEmail())
                        .roles(account.getRolesAsString())
                        .build())
                .build();
    }

    private String exchangeCodeForToken(String code) {
        try {
            log.info("Exchanging code for token...");
            log.info("Client ID: {}", clientId);
            log.info("Redirect URI: {}", redirectUri);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("redirect_uri", redirectUri);
            params.add("grant_type", "authorization_code");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    TOKEN_URL,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body == null || !body.containsKey("access_token")) {
                throw new UnauthorizedException("Failed to get access token from Google");
            }

            return (String) body.get("access_token");
        } catch (HttpClientErrorException e) {
            // Log chi tiết response từ Google
            log.error("Google OAuth error - Status: {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new UnauthorizedException("Failed to authenticate with Google");
        } catch (Exception e) {
            log.error("Error exchanging code for token: {}", e.getMessage(), e);
            throw new UnauthorizedException("Failed to authenticate with Google");
        }
    }

    private GoogleUserInfo fetchUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    USER_INFO_URL,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new UnauthorizedException("Failed to get user info from Google");
            }

            return GoogleUserInfo.builder()
                    .id((String) body.get("id"))
                    .email((String) body.get("email"))
                    .name((String) body.get("name"))
                    .picture((String) body.get("picture"))
                    .verifiedEmail((Boolean) body.get("verified_email"))
                    .build();
        } catch (Exception e) {
            log.error("Error fetching user info: {}", e.getMessage(), e);
            throw new UnauthorizedException("Failed to get user info from Google");
        }
    }

    private Account createOrLinkAccount(GoogleUserInfo googleUser) {
        Account account = accountRepository.findByEmail(googleUser.getEmail())
                .orElse(null);

        boolean isNewAccount = false;

        if (account == null) {
            String username = generateUniqueUsername(googleUser.getEmail());

            account = Account.builder()
                    .username(username)
                    .email(googleUser.getEmail())
                    .password(UUID.randomUUID().toString())
                    .roles(Set.of(Role.USER))
                    .status(AccountStatus.ACTIVE)
                    .emailVerified(true)
                    .build();

            account = accountRepository.save(account);
            isNewAccount = true;

            log.info("New account created for Google user: {}", account.getUsername());
        }

        AuthIdentity authIdentity = AuthIdentity.builder()
                .account(account)
                .provider(Provider.GOOGLE)
                .providerId(googleUser.getId())
                .providerEmail(googleUser.getEmail())
                .build();

        authIdentityRepository.save(authIdentity);

        log.info("Google identity linked to account: {}", account.getUsername());

        if (isNewAccount) {
            try {
                CreateUserRequest createUserRequest = CreateUserRequest.builder()
                        .accountId(account.getId())
                        .username(account.getUsername())
                        .email(account.getEmail())
                        .build();

                userServiceClient.createUser(createUserRequest);
                log.info("User created in user-service: {}", account.getId());
            } catch (Exception e) {
                log.error("Failed to create user in user-service: {}", e.getMessage(), e);
            }

            try {
                accountEventPublisher.publishAccountVerifiedEvent(account);
            } catch (Exception e) {
                log.error("Failed to publish account verification event: {}", e.getMessage(), e);
            }
        }

        return account;
    }

    private String generateUniqueUsername(String email) {
        String baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
        String username = baseUsername;
        int counter = 1;

        while (accountRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }
}