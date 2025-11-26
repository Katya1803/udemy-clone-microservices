package com.app.auth.repository;

import com.app.auth.constant.Provider;
import com.app.auth.entity.AuthIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthIdentityRepository extends JpaRepository<AuthIdentity, String> {

    Optional<AuthIdentity> findByProviderAndProviderId(Provider provider, String providerId);
}