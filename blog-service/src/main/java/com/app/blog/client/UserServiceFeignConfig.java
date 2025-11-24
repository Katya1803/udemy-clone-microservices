package com.app.blog.client;

import com.app.common.client.BaseOAuth2TokenProvider;
import com.app.common.feign.OAuth2FeignRequestInterceptor;
import feign.Logger;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class UserServiceFeignConfig {

    private final BaseOAuth2TokenProvider tokenProvider;

    @Bean
    public RequestInterceptor userServiceRequestInterceptor() {
        return new OAuth2FeignRequestInterceptor(tokenProvider, "user-service");
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}