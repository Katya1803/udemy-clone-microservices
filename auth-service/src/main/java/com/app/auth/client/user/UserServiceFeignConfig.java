package com.app.auth.client.user;

import com.app.common.feign.OAuth2FeignRequestInterceptor;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class UserServiceFeignConfig {

    private final UserServiceTokenProvider tokenProvider;

    @Bean
    public RequestInterceptor userServiceRequestInterceptor() {
        return new OAuth2FeignRequestInterceptor(tokenProvider, "user-service");
    }
}