package com.codeastras.backend.codeastras.dto.chat;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class CallParticipantsMessage {
    private UUID projectId;
    private Set<UUID> participants;

    public CallParticipantsMessage(UUID projectId, Set<UUID> participants) {
    }
}
