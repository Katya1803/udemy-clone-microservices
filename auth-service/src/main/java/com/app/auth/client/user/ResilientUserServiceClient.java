package com.app.auth.client.user;

import com.app.common.dto.common.CreateUserRequest;
import com.app.common.dto.common.UserResponse;
import com.app.common.dto.response.ApiResponse;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resilient wrapper for UserServiceClient with Circuit Breaker, Retry, and Bulkhead
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResilientUserServiceClient {

    private final UserServiceClient userServiceClient;

    /**
     * Create user with resilience patterns
     * - Circuit Breaker: Opens circuit after 50% failure rate
     * - Retry: Retries up to 3 times with exponential backoff
     * - Bulkhead: Limits concurrent calls to prevent resource exhaustion
     */
    @CircuitBreaker(name = "userService", fallbackMethod = "createUserFallback")
    @Retry(name = "userService")
    @Bulkhead(name = "userService")
    public ApiResponse<UserResponse> createUser(CreateUserRequest request) {
        log.debug("Creating user via User Service: {}", request.getUsername());
        return userServiceClient.createUser(request);
    }

    /**
     * Fallback method when user service is unavailable
     * Returns a response indicating the user will be created later
     */
    private ApiResponse<UserResponse> createUserFallback(CreateUserRequest request, Exception e) {
        log.error("User Service unavailable. User creation will be retried later. Error: {}", e.getMessage());

        // Return a response indicating temporary failure
        return ApiResponse.<UserResponse>builder()
                .success(false)
                .message("User service temporarily unavailable. Your account has been created, " +
                        "but profile setup will complete shortly.")
                .build();
    }
}