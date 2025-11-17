package com.app.auth.entity;

import com.app.auth.constant.AccountStatus;
import com.app.auth.constant.Role;
import com.app.common.util.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "account_roles", joinColumns = @JoinColumn(name = "account_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AccountStatus status = AccountStatus.PENDING_VERIFICATION;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AuthIdentity> providers = new HashSet<>();


    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public boolean isPendingVerification() {
        return status == AccountStatus.PENDING_VERIFICATION;
    }

    public void activate() {
        this.status = AccountStatus.ACTIVE;
        this.emailVerified = true;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    public boolean hasRole(Role role) {
        return this.roles.contains(role);
    }

    public String getRolesAsString() {
        return roles.stream()
                .map(Role::name)
                .map(role -> "ROLE_" + role) // Add ROLE_ prefix
                .collect(Collectors.joining(","));
    }

    public static Set<Role> parseRoles(String rolesStr) {
        if (rolesStr == null || rolesStr.isBlank()) {
            return Set.of(Role.USER);
        }

        return Set.of(rolesStr.split(","))
                .stream()
                .map(r -> r.replace("ROLE_", "").trim())
                .map(Role::valueOf)
                .collect(Collectors.toSet());
    }
}