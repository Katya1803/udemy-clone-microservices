package com.app.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Stores OAuth2 client credentials for service-to-service authentication
 */
@Entity
@Table(name = "service_clients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceClient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "client_id", unique = true, nullable = false, length = 100)
    private String clientId;

    @Column(name = "client_secret", nullable = false)
    private String clientSecret;

    @Column(name = "allowed_scopes", nullable = false)
    private String allowedScopes; // Comma-separated: user:read,user:write

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();


    public String[] getScopesArray() {
        return allowedScopes != null ? allowedScopes.split(",") : new String[0];
    }

    public boolean hasScope(String scope) {
        if (allowedScopes == null) return false;
        return allowedScopes.contains(scope);
    }
}