package com.app.auth.client.email;

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
public class EmailServiceFeignConfig {

    private final EmailServiceTokenProvider tokenProvider;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    @Bean
    public RequestInterceptor emailServiceRequestInterceptor() {
        return new OAuth2FeignRequestInterceptor(tokenProvider, "email-service");
    }

    /**
     * Configure circuit breaker event listeners
     * Use @PostConstruct instead of @Bean for void methods
     */
    @PostConstruct
    public void configureEmailServiceCircuitBreaker() {
        var circuitBreaker = circuitBreakerRegistry.circuitBreaker("emailService");

        circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                        log.info("Email Service Circuit Breaker state changed: {}", event))
                .onError(event ->
                        log.error("Email Service Circuit Breaker error: {}", event))
                .onSuccess(event ->
                        log.debug("Email Service Circuit Breaker success"));
    }

    /**
     * Configure retry event listeners
     */
    @PostConstruct
    public void configureEmailServiceRetry() {
        var retry = retryRegistry.retry("emailService");

        retry.getEventPublisher()
                .onRetry(event ->
                        log.warn("Retrying Email Service call. Attempt: {}", event.getNumberOfRetryAttempts()))
                .onError(event ->
                        log.error("Email Service retry failed after {} attempts", event.getNumberOfRetryAttempts()));
    }
}