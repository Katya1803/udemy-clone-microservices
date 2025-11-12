package com.app.auth.client.user;

import com.app.common.feign.OAuth2FeignRequestInterceptor;
import feign.RequestInterceptor;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class UserServiceFeignConfig {

    private final UserServiceTokenProvider tokenProvider;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    @Bean
    public RequestInterceptor userServiceRequestInterceptor() {
        return new OAuth2FeignRequestInterceptor(tokenProvider, "user-service");
    }

    /**
     * Configure circuit breaker event listeners
     * Use @PostConstruct instead of @Bean for void methods
     */
    @PostConstruct
    public void configureUserServiceCircuitBreaker() {
        var circuitBreaker = circuitBreakerRegistry.circuitBreaker("userService");

        circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                        log.info("User Service Circuit Breaker state changed: {}", event))
                .onError(event ->
                        log.error("User Service Circuit Breaker error: {}", event))
                .onSuccess(event ->
                        log.debug("User Service Circuit Breaker success"));
    }

    /**
     * Configure retry event listeners
     */
    @PostConstruct
    public void configureUserServiceRetry() {
        var retry = retryRegistry.retry("userService");

        retry.getEventPublisher()
                .onRetry(event ->
                        log.warn("Retrying User Service call. Attempt: {}", event.getNumberOfRetryAttempts()))
                .onError(event ->
                        log.error("User Service retry failed after {} attempts", event.getNumberOfRetryAttempts()));
    }
}