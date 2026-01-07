package com.codeastras.backend.codeastras.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PresenceEvent {
    private PresenceEventType type;
    private UUID userId;
    private UUID projectId;
    private UUID fileId; // nullable
    private Instant timestamp;
}

