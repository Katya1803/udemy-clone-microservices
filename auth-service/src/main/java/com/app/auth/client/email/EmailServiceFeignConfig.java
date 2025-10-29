package com.app.auth.client.email;

import com.app.common.feign.OAuth2FeignRequestInterceptor;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class EmailServiceFeignConfig {

    private final EmailServiceTokenProvider tokenProvider;

    @Bean
    public RequestInterceptor emailServiceRequestInterceptor() {
        return new OAuth2FeignRequestInterceptor(tokenProvider, "email-service");
    }
}