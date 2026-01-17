package com.codeastras.backend.codeastras.repository.auth;

import com.codeastras.backend.codeastras.entity.auth.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @Modifying
    @Query("""
        update RefreshToken rt
        set rt.revoked = true
        where rt.sessionId = :sessionId
    """)
    int revokeBySessionId(String sessionId);

    Optional<RefreshToken> findBySessionIdAndRevokedFalse(String sessionId);
}
