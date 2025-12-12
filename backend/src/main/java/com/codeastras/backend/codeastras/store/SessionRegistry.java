package com.codeastras.backend.codeastras.store;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {

    private final Logger log = LoggerFactory.getLogger(SessionRegistry.class);

    // sessionId -> SessionInfo
    private final Map<String, SessionInfo> bySessionId = new ConcurrentHashMap<>();

    // projectId -> SessionInfo
    private final Map<UUID, SessionInfo> byProjectId = new ConcurrentHashMap<>();

    // projectId -> connected users
    private final Map<UUID, Set<UUID>> connectedUsers = new ConcurrentHashMap<>();

    @Getter
    @Setter
    public static class SessionInfo {
        public final String sessionId;
        public final String containerName;
        public final UUID projectId;
        public final UUID ownerUserId;
        public Instant createdAt;
        public Instant lastSeen;

        public SessionInfo(String sessionId,
                           String containerName,
                           UUID projectId,
                           UUID ownerUserId) {
            this.sessionId = sessionId;
            this.containerName = containerName;
            this.projectId = projectId;
            this.ownerUserId = ownerUserId;
            this.createdAt = Instant.now();
            this.lastSeen = Instant.now();
        }
    }


    // ---------------- SESSION MANAGEMENT ----------------

    /** Register a session for a project. */
    public void register(UUID projectId, String sessionId, String containerName, UUID ownerUserId) {

        SessionInfo info = new SessionInfo(sessionId, containerName, projectId, ownerUserId);

        bySessionId.put(sessionId, info);
        byProjectId.put(projectId, info);
        connectedUsers.putIfAbsent(projectId, ConcurrentHashMap.newKeySet());

        log.info("Registered session {} for project {} (owner={})",
                sessionId, projectId, ownerUserId);
    }

    /** Remove a session */
    public void remove(String sessionId) {
        SessionInfo info = bySessionId.remove(sessionId);
        if (info != null) {
            byProjectId.remove(info.projectId);
            connectedUsers.remove(info.projectId);
            log.info("Removed session {} for project {}", sessionId, info.projectId);
        }
    }

    /** Lookup session by ID */
    public Optional<SessionInfo> get(String sessionId) {
        return Optional.ofNullable(bySessionId.get(sessionId));
    }

    /** Lookup session by project */
    public SessionInfo getByProject(UUID projectId) {
        return byProjectId.get(projectId);
    }

    public String getSessionIdByProject(UUID projectId) {
        SessionInfo info = byProjectId.get(projectId);
        return (info == null) ? null : info.sessionId;
    }

    public Optional<String> getSessionIdForProject(UUID projectId) {
        return Optional.ofNullable(getSessionIdByProject(projectId));
    }

    // ---------------- USER CONNECTION TRACKING ----------------

    public void registerUserConnection(UUID projectId, UUID userId) {
        connectedUsers.computeIfAbsent(projectId, k -> ConcurrentHashMap.newKeySet())
                .add(userId);

        SessionInfo s = byProjectId.get(projectId);
        if (s != null) {
            s.lastSeen = Instant.now();
        }

        log.debug("User {} connected to project {}", userId, projectId);
    }

    public void unregisterUserConnection(UUID projectId, UUID userId) {
        Set<UUID> set = connectedUsers.get(projectId);
        if (set != null) {
            set.remove(userId);
        }
        log.debug("User {} disconnected from project {}", userId, projectId);
    }

    public Set<UUID> getConnectedUsers(UUID projectId) {
        return connectedUsers.getOrDefault(projectId, Collections.emptySet());
    }

    public Set<UUID> getActiveProjects() {
        return new HashSet<>(byProjectId.keySet());
    }
}
