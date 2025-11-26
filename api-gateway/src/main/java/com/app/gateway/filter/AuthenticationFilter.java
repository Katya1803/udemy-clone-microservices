package com.app.gateway.filter;

import com.app.gateway.security.GatewayTokenValidator;
import com.app.gateway.security.GatewayConstants;
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
            "/auth/login",
            "/auth/register",
            "/auth/verify-otp",
            "/auth/resend-otp",
            "/auth/refresh",
            "/auth/google/callback"
    );

    // Paths that are public for GET requests only
    private static final List<String> PUBLIC_READ_PATHS = List.of(
            "/api/blogs/series",
            "/api/blogs/posts",
            "/api/blogs/posts/slug",
            "/api/courses"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String method = request.getMethod().name();

        if (method.equalsIgnoreCase("OPTIONS")) {
            return chain.filter(exchange);
        }

        log.debug("Processing request: {} {}", method, path);

        // Check if path is always public (any method)
        if (isPublicPath(path)) {
            log.debug("Public path accessed, skipping authentication: {}", path);
            return chain.filter(exchange);
        }

        // Check if path is public for GET requests only
        if (isPublicReadPath(path) && method.equalsIgnoreCase("GET")) {
            log.debug("Public read path accessed with GET, skipping authentication: {}", path);
            return chain.filter(exchange);
        }

        log.debug("Protected path, authentication required: {} {}", method, path);

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
                                .header(GatewayConstants.HEADER_ACCOUNT_ID, userId)
                                .header(GatewayConstants.HEADER_ACCOUNT_ROLE, String.join(",", roles))
                                .header(GatewayConstants.HEADER_ACCOUNT_EMAIL, email)
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

    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(GatewayConstants.JWT_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(GatewayConstants.JWT_PREFIX)) {
            return bearerToken.substring(GatewayConstants.JWT_PREFIX.length());
        }

        return null;
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isPublicReadPath(String path) {
        return PUBLIC_READ_PATHS.stream().anyMatch(path::startsWith);
    }

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