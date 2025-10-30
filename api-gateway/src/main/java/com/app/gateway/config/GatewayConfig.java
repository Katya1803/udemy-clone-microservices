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

                // Example service
                .route("test-service", r -> r
                        .path("/api/test/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .retry(config -> config.setRetries(2))
                        )
                        .uri("lb://test-service")
                )

                .build();
    }
}
