package com.app.auth.entity;

import com.app.auth.constant.AccountStatus;
import com.app.auth.constant.Role;
import com.app.common.util.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role roles;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AccountStatus status = AccountStatus.PENDING_VERIFICATION;

    @Column(name = "email_verified",nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.Set<AuthIdentity> providers = new java.util.HashSet<>();


    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public boolean isPendingVerification() {
        return status == AccountStatus.PENDING_VERIFICATION;
    }

    public void active() {
        this.status = AccountStatus.ACTIVE;
        this.emailVerified = true;
    }
}
