package com.codeastras.backend.codeastras.repository;


import com.codeastras.backend.codeastras.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findBySessionId(String sessionId);
    void deleteByUserId(UUID userId);

}
