package com.app.auth.config;

import com.app.auth.entity.ServiceClient;
import com.app.auth.repository.ServiceClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class ServiceClientInitializer {

    private final ServiceClientRepository serviceClientRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initServiceClients() {
        return args -> {
            log.info("Initializing service clients...");

            // Auth Service Client (for calling email-service)
            if (!serviceClientRepository.existsByClientId("auth-service")) {
                ServiceClient authServiceClient = ServiceClient.builder()
                        .clientId("auth-service")
                        .clientSecret(passwordEncoder.encode("auth-service-secret"))
                        .allowedScopes("email:send")
                        .enabled(true)
                        .build();

                serviceClientRepository.save(authServiceClient);
                log.info("✅ Created service client: auth-service");
            } else {
                log.info("✓ Service client 'auth-service' already exists");
            }

            // Example: User Service Client
            /*
            if (!serviceClientRepository.existsByClientId("user-service")) {
                ServiceClient userServiceClient = ServiceClient.builder()
                        .clientId("user-service")
                        .clientSecret(passwordEncoder.encode("user-service-secret"))
                        .allowedScopes("auth:validate,email:send")
                        .enabled(true)
                        .build();

                serviceClientRepository.save(userServiceClient);
                log.info("✅ Created service client: user-service");
            }
            */

            log.info("Service clients initialization completed");
        };
    }
}