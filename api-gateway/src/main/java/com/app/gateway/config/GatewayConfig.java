package com.app.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        log.info("Configuring Gateway routes");

        return builder.routes()
                // Auth Service - No prefix stripping
                .route("auth-service", r -> r
                        .path("/auth/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .retry(config -> config
                                        .setRetries(3)
                                        .setMethods(org.springframework.http.HttpMethod.GET,
                                                org.springframework.http.HttpMethod.POST)
                                )
                        )
                        .uri("lb://auth-service")
                )

                // User Service - Strip /api prefix
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .retry(config -> config.setRetries(2))
                        )
                        .uri("lb://user-service")
                )

                // Blog Service - Strip /api prefix
                .route("blog-service", r -> r
                        .path("/api/blogs/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .retry(config -> config.setRetries(2))
                        )
                        .uri("lb://blog-service")
                )

                // Course Service - Strip /api prefix
                .route("course-service", r -> r
                        .path("/api/courses/**", "/api/enrollments/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .retry(config -> config.setRetries(2))
                        )
                        .uri("lb://course-service")
                )

                .build();
    }
}