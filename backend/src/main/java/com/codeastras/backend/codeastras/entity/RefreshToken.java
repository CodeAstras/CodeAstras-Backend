package com.codeastras.backend.codeastras.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_user", columnList = "user_id"),
        @Index(name = "idx_refresh_session", columnList = "session_id", unique = true)
})
@Getter
@Setter
public class RefreshToken {

    @Id
    private UUID id;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String sessionId;

    @Column(nullable = false)
    private String tokenHash;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    private String userAgent;
    private String ip;
}
