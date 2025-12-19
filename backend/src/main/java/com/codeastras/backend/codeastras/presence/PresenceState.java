package com.codeastras.backend.codeastras.presence;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class PresenceState {
    private UUID userId;
    private UUID projectId;
    private UUID fileId;
    private Instant lastSeen;

    public PresenceState(UUID userId, UUID projectId, Object o, Instant now) {
    }
}
