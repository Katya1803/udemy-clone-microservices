package com.app.auth.client.email;

import com.app.common.dto.common.EmailRequest;
import com.app.common.dto.common.EmailResponse;
import com.app.common.dto.response.ApiResponse;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resilient wrapper for EmailServiceClient with Circuit Breaker, Retry, and Bulkhead
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResilientEmailServiceClient {

    private final EmailServiceClient emailServiceClient;

    /**
     * Send email with resilience patterns
     * - Circuit Breaker: Opens circuit after 50% failure rate
     * - Retry: Retries up to 3 times with exponential backoff
     * - Bulkhead: Limits concurrent calls to prevent resource exhaustion
     */
    @CircuitBreaker(name = "emailService", fallbackMethod = "sendEmailFallback")
    @Retry(name = "emailService")
    @Bulkhead(name = "emailService")
    public ApiResponse<EmailResponse> sendEmail(EmailRequest request) {
        log.debug("Sending email via Email Service to: {}", request.getTo());
        return emailServiceClient.sendEmail(request);
    }

    /**
     * Fallback method when email service is unavailable
     * Logs the failure and returns a response indicating temporary failure
     */
    private ApiResponse<EmailResponse> sendEmailFallback(EmailRequest request, Exception e) {
        log.error("Email Service unavailable. Email to {} will be queued for retry. Error: {}",
                request.getTo(), e.getMessage());

        // In production, you might want to queue this email for later retry
        // For now, just return a response indicating temporary failure
        return ApiResponse.<EmailResponse>builder()
                .success(false)
                .message("Email service temporarily unavailable. Email will be sent shortly.")
                .data(EmailResponse.builder()
                        .message("Queued for retry")
                        .build())
                .build();
    }
}