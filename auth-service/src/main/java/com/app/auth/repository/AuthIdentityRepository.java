package com.app.auth.repository;

import com.app.auth.entity.AuthIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthIdentityRepository extends JpaRepository<AuthIdentity, String> {
}
