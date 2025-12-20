package com.codeastras.backend.codeastras.presence;

import com.codeastras.backend.codeastras.store.SessionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PresenceManager {

    private final SessionRegistry sessionRegistry;

    public void userJoined(UUID projectId, UUID userId) {
        sessionRegistry.userJoined(projectId, userId);
    }

    public void userLeft(UUID projectId, UUID userId) {
        sessionRegistry.userLeft(projectId, userId);
    }

    public void updateFile(UUID projectId, UUID userId, UUID fileId) {
        sessionRegistry.updateFile(projectId, userId, fileId);
    }

    public Collection<SessionRegistry.PresenceInfo> getPresenceSnapshot(UUID projectId) {
        return sessionRegistry.getPresenceSnapshot(projectId);
    }
}
