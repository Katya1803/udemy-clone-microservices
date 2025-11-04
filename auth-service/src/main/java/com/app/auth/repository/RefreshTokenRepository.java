package com.app.auth.repository;

import com.app.auth.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

    Optional<RefreshToken> findById(String tokenHash);

    List<RefreshToken> findByAccountId(String accountId);

    void deleteByAccountId(String userId);
}