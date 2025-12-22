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
        require(projectId, "projectId");
        return sessionRegistry.getSessionIdForProject(projectId);
    }

    public SessionRegistry.SessionInfo getSessionByProject(UUID projectId) {
        require(projectId, "projectId");
        return sessionRegistry.getByProject(projectId);
    }

    public SessionRegistry.SessionInfo getSessionById(String sessionId) {
        require(sessionId, "sessionId");
        return sessionRegistry.getBySessionId(sessionId);
    }

    // ================= PRESENCE =================

    /**
     * Explicit user intent (UI-driven join).
     *
     * @return true if this is a NEW join, false if already present
     */
    public boolean userJoined(UUID projectId, UUID userId) {
        require(projectId, "projectId");
        require(userId, "userId");
        return sessionRegistry.userJoined(projectId, userId);
    }

    /**
     * Explicit user intent (UI-driven leave).
     *
     * @return true if user was present and removed
     */
    public boolean userLeft(UUID projectId, UUID userId) {
        require(projectId, "projectId");
        require(userId, "userId");
        return sessionRegistry.userLeft(projectId, userId);
    }

    /**
     * WebSocket lifecycle disconnect handler.
     * This is the ONLY method that should be used on WS disconnect.
     */
    public void wsUserLeft(String wsSessionId) {
        require(wsSessionId, "wsSessionId");
        sessionRegistry.wsUserLeft(wsSessionId);
    }

    // ================= FILE / HEARTBEAT =================

    /**
     * Update active file for a user.
     *
     * @return true if file actually changed
     */
    public boolean updateFile(UUID projectId, UUID userId, UUID fileId) {
        require(projectId, "projectId");
        require(userId, "userId");
        return sessionRegistry.updateFile(projectId, userId, fileId);
    }

    /**
     * Heartbeat to keep user presence alive.
     */
    public void heartbeat(UUID projectId, UUID userId) {
        require(projectId, "projectId");
        require(userId, "userId");
        sessionRegistry.heartbeat(projectId, userId);
    }

    /**
     * Snapshot for late joiners / resync.
     */
    public Collection<SessionRegistry.PresenceInfo> getPresenceSnapshot(UUID projectId) {
        require(projectId, "projectId");
        return sessionRegistry.getPresenceSnapshot(projectId);
    }

    /**
     * HARD cleanup: logout / token revoked / account deleted.
     */
    public void userLeftEverywhere(UUID userId) {
        require(userId, "userId");
        sessionRegistry.userLeftEverywhere(userId);
    }

    // ================= INTERNAL =================

    private void require(Object value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
    }
}
