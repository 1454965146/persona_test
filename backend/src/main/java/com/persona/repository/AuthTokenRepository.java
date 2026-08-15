package com.persona.repository;

import com.persona.model.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {
    Optional<AuthToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("delete from AuthToken t where t.revoked = true or t.expiresAt < :now")
    int deleteRevokedOrExpired(@Param("now") LocalDateTime now);
}
