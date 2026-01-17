package com.codeastras.backend.codeastras.service.chat;

import java.util.Set;
import java.util.UUID;

public interface CallService {

    Set<UUID> joinCall(UUID projectId, UUID userId);

    void leaveCall(UUID projectId, UUID userId);

    boolean isParticipant(UUID projectId, UUID userId);

    void forceLeave(UUID projectId, UUID userId);
}
