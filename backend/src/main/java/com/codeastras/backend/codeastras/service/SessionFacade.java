package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.store.SessionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionFacade {

    private final SessionRegistry sessionRegistry;

    // ================= SESSION =================

    public Optional<String> getSessionIdForProject(UUID projectId) {
        return sessionRegistry.getSessionIdForProject(projectId);
    }

    public SessionRegistry.SessionInfo getSessionByProject(UUID projectId) {
        return sessionRegistry.getByProject(projectId);
    }

    public SessionRegistry.SessionInfo getSessionById(String sessionId) {
        return sessionRegistry.getBySessionId(sessionId);
    }

    // ================= PRESENCE =================

    /**
     * Explicit user intent (UI-driven join)
     */
    public void userJoined(UUID projectId, UUID userId) {
        if (projectId == null || userId == null) return;
        sessionRegistry.userJoined(projectId, userId);
    }

    /**
     * Explicit user intent (UI-driven leave).
     * ❗ DO NOT call this from WebSocket disconnect listeners.
     */
    public void userLeft(UUID projectId, UUID userId) {
        if (projectId == null || userId == null) return;
        sessionRegistry.userLeft(projectId, userId);
    }

    /**
     * WebSocket lifecycle disconnect handler.
     * This is the ONLY method that should be used on WS disconnect.
     */
    public void wsUserLeft(String wsSessionId) {
        if (wsSessionId == null) return;
        sessionRegistry.wsUserLeft(wsSessionId);
    }

    // ================= FILE / HEARTBEAT =================

    public void updateFile(UUID projectId, UUID userId, UUID fileId) {
        if (projectId == null || userId == null) return;
        sessionRegistry.updateFile(projectId, userId, fileId);
    }

    public void heartbeat(UUID projectId, UUID userId) {
        if (projectId == null || userId == null) return;
        sessionRegistry.heartbeat(projectId, userId);
    }

    public Collection<SessionRegistry.PresenceInfo> getPresenceSnapshot(UUID projectId) {
        return sessionRegistry.getPresenceSnapshot(projectId);
    }

    public void userLeftEverywhere(UUID userId) {
        sessionRegistry.userLeftEverywhere(userId);
    }

}
