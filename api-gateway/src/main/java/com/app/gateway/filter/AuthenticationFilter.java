package com.app.gateway.filter;

import com.app.gateway.security.GatewayTokenValidator;
import com.app.gateway.security.SecurityConstants;
import com.app.gateway.service.GatewayTokenBlacklist;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final GatewayTokenValidator jwtTokenValidator;
    private final GatewayTokenBlacklist tokenBlacklistService;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/",
            "/test/public"
    );


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        log.debug("Processing request: {} {}", request.getMethod(), path);

        if (isPublicPath(path)) {
            log.debug("Public path accessed, skipping authentication: {}", path);
            return chain.filter(exchange);
        }

        log.debug("Protected path, authentication required: {}", path);

        String token = extractToken(request);

        if (!StringUtils.hasText(token)) {
            log.warn("No token provided for protected path: {}", path);
            return onError(exchange, "Missing authentication token", HttpStatus.UNAUTHORIZED);
        }

        try {
            jwtTokenValidator.validateToken(token);
            log.debug("Token validated successfully");

            return tokenBlacklistService.isBlacklisted(token)
                    .flatMap(isBlacklisted -> {
                        if (isBlacklisted) {
                            log.warn("Blacklisted token detected for path: {}", path);
                            return onError(exchange, "Token has been revoked", HttpStatus.UNAUTHORIZED);
                        }

                        String userId = jwtTokenValidator.getUserId(token);
                        String roles = jwtTokenValidator.getRole(token);
                        String email = jwtTokenValidator.getEmail(token);

                        log.debug("Authenticated user: {} with roles: {}", userId, roles);

                        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                .header(SecurityConstants.HEADER_USER_ID, userId)
                                .header(SecurityConstants.HEADER_USER_ROLES, String.join(",", roles))
                                .header(SecurityConstants.HEADER_USER_EMAIL, email)
                                .build();

                        ServerWebExchange modifiedExchange = exchange.mutate()
                                .request(modifiedRequest)
                                .build();

                        return chain.filter(modifiedExchange);
                    })
                    .onErrorResume(error -> {
                        log.error("Error checking token blacklist: {}", error.getMessage());
                        return onError(exchange, "Authentication failed", HttpStatus.UNAUTHORIZED);
                    });

        } catch (GatewayTokenValidator.InvalidTokenException e) {
            log.error("Invalid token: {}", e.getMessage());
            return onError(exchange, e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage(), e);
            return onError(exchange, "Authentication failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(SecurityConstants.JWT_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(SecurityConstants.JWT_PREFIX)) {
            return bearerToken.substring(SecurityConstants.JWT_PREFIX.length());
        }

        return null;
    }

    /**
     * Check if path is public (no auth required)
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * Handle authentication errors
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");

        String errorResponse = String.format(
                "{\"success\":false,\"error\":{\"code\":\"%s\",\"message\":\"%s\"},\"timestamp\":\"%s\"}",
                status.name(),
                message,
                java.time.Instant.now()
        );

        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes()))
        );
    }

    @Override
    public int getOrder() {
        return -100;
    }
}