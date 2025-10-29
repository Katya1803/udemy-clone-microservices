package com.app.auth.repository;

import com.app.auth.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("SELECT a FROM Account a WHERE a.username = :value OR a.email = :value")
    Optional<Account> findByUsernameOrEmail(@Param("value") String value);

}
