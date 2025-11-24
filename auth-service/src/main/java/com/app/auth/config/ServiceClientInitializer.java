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

            // Auth Service Client
            if (!serviceClientRepository.existsByClientId("auth-service")) {
                ServiceClient authServiceClient = ServiceClient.builder()
                        .clientId("auth-service")
                        .clientSecret(passwordEncoder.encode("auth-service-secret"))
                        .allowedScopes("email:send,user:create")
                        .enabled(true)
                        .build();
                serviceClientRepository.save(authServiceClient);
                log.info("✅ Created service client: auth-service");
            } else {
                log.info("✓ Service client 'auth-service' already exists");
            }

            // Blog Service Client - ADD THIS
            if (!serviceClientRepository.existsByClientId("blog-service")) {
                ServiceClient blogServiceClient = ServiceClient.builder()
                        .clientId("blog-service")
                        .clientSecret(passwordEncoder.encode("blog-service-secret"))
                        .allowedScopes("user:read")
                        .enabled(true)
                        .build();
                serviceClientRepository.save(blogServiceClient);
                log.info("✅ Created service client: blog-service");
            } else {
                log.info("✓ Service client 'blog-service' already exists");
            }

            log.info("Service clients initialization completed");
        };
    }
}