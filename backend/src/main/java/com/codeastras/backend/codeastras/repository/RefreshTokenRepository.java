package com.codeastras.backend.codeastras.repository;


import com.codeastras.backend.codeastras.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findBySessionId(String sessionId);
    void deleteByUserId(UUID userId);

}
