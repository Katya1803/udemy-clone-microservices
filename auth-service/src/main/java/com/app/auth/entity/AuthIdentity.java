package com.app.auth.entity;

import com.app.auth.constant.Provider;
import com.app.common.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "auth_identities",
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_user_id"}))
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthIdentity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_auth_identity_account"))
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider",nullable = false)
    private Provider provider;

    @Column(name = "provider_user_id", nullable = false, length = 191)
    private String providerId;

    @Column(name = "provider_email", length = 191)
    private String providerEmail;
}
