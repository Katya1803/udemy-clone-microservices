package com.app.auth.controller;

import com.app.auth.dto.OAuth2TokenRequest;
import com.app.auth.service.OAuth2ClientService;
import com.app.common.dto.auth.OAuth2TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OAuth2TokenController {

    private final OAuth2ClientService oauth2ClientService;

    @PostMapping(
            value = "/oauth/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<OAuth2TokenResponse> token(@RequestParam MultiValueMap<String, String> form) {
        OAuth2TokenRequest req = OAuth2TokenRequest.builder()
                .grantType(form.getFirst("grant_type"))
                .clientId(form.getFirst("client_id"))
                .clientSecret(form.getFirst("client_secret"))
                .scope(form.getFirst("scope"))
                .audience(form.getFirst("audience"))
                .build();

        OAuth2TokenResponse result = oauth2ClientService.generateServiceToken(req);
        return ResponseEntity.ok(result);
    }

    @PostMapping(
            value = "/oauth/token",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<OAuth2TokenResponse> tokenJson(@RequestBody OAuth2TokenRequest req) {
        OAuth2TokenResponse result = oauth2ClientService.generateServiceToken(req);
        return ResponseEntity.ok(result);
    }
}
