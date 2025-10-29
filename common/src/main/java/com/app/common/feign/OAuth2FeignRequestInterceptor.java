package com.app.common.feign;

import com.app.common.client.BaseOAuth2TokenProvider;
import com.app.common.constant.SecurityConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class OAuth2FeignRequestInterceptor implements RequestInterceptor {

    private final BaseOAuth2TokenProvider tokenProvider;
    private final String audience;

    @Override
    public void apply(RequestTemplate template) {
        try {
            // Get service token for target audience
            String token = tokenProvider.getServiceToken(audience);

            // Add Authorization header
            template.header(
                    SecurityConstants.JWT_HEADER,
                    SecurityConstants.JWT_PREFIX + token
            );

            log.debug("Added service token for audience: {}", audience);

        } catch (Exception e) {
            log.error("Failed to add service token to Feign request for audience: {}", audience, e);
            throw new RuntimeException("Failed to obtain service token", e);
        }
    }
}