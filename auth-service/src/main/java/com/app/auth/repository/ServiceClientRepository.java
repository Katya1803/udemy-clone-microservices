package com.app.auth.repository;

import com.app.auth.entity.ServiceClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceClientRepository extends JpaRepository<ServiceClient, String> {

    Optional<ServiceClient> findByClientId(String clientId);

    boolean existsByClientId(String clientId);
}