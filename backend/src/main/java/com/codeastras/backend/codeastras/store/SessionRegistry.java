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

    private static final long PRESENCE_TTL_SECONDS = 30;

    // sessionId -> SessionInfo
    private final Map<String, SessionInfo> bySessionId = new ConcurrentHashMap<>();

    // projectId -> SessionInfo
    private final Map<UUID, SessionInfo> byProjectId = new ConcurrentHashMap<>();

    // projectId -> connected users
    private final Map<UUID, Set<UUID>> connectedUsers = new ConcurrentHashMap<>();

    // projectId -> (userId -> presence)
    private final Map<UUID, Map<UUID, PresenceInfo>> presence = new ConcurrentHashMap<>();

    // ================= MODELS =================

    @Getter
    @Setter
    public static class SessionInfo {
        private final String sessionId;
        private final String containerName;
        private final UUID projectId;
        private final UUID ownerUserId;
        private Instant createdAt;
        private Instant lastSeen;

        public SessionInfo(
                String sessionId,
                String containerName,
                UUID projectId,
                UUID ownerUserId
        ) {
            this.sessionId = sessionId;
            this.containerName = containerName;
            this.projectId = projectId;
            this.ownerUserId = ownerUserId;
            this.createdAt = Instant.now();
            this.lastSeen = Instant.now();
        }
    }

    public record PresenceInfo(
            UUID userId,
            UUID fileId,
            Instant lastSeen
    ) {}

    // ================= SESSION MANAGEMENT =================

    public void register(
            UUID projectId,
            String sessionId,
            String containerName,
            UUID ownerUserId
    ) {
        SessionInfo info =
                new SessionInfo(sessionId, containerName, projectId, ownerUserId);

        bySessionId.put(sessionId, info);
        byProjectId.put(projectId, info);
        connectedUsers.putIfAbsent(projectId, ConcurrentHashMap.newKeySet());

        log.info("Registered session {} for project {}", sessionId, projectId);
    }

    public void remove(String sessionId) {
        SessionInfo info = bySessionId.remove(sessionId);
        if (info != null) {
            byProjectId.remove(info.getProjectId());
            connectedUsers.remove(info.getProjectId());
            presence.remove(info.getProjectId());
            log.info("Removed session {}", sessionId);
        }
    }

    // ================= SESSION LOOKUPS =================

    public SessionInfo getBySessionId(String sessionId) {
        return bySessionId.get(sessionId);
    }

    public SessionInfo getByProject(UUID projectId) {
        return byProjectId.get(projectId);
    }

    public Optional<String> getSessionIdForProject(UUID projectId) {
        SessionInfo info = byProjectId.get(projectId);
        return info == null
                ? Optional.empty()
                : Optional.of(info.getSessionId());
    }

    // ================= PRESENCE =================

    public void userJoined(UUID projectId, UUID userId) {
        presence
                .computeIfAbsent(projectId, k -> new ConcurrentHashMap<>())
                .put(userId, new PresenceInfo(userId, null, Instant.now()));

        connectedUsers
                .computeIfAbsent(projectId, k -> ConcurrentHashMap.newKeySet())
                .add(userId);

        log.debug("User {} joined project {}", userId, projectId);
    }

    public void userLeft(UUID projectId, UUID userId) {
        Map<UUID, PresenceInfo> users = presence.get(projectId);
        if (users != null) {
            users.remove(userId);
            if (users.isEmpty()) {
                presence.remove(projectId);
                connectedUsers.remove(projectId);
            }
        }
        log.debug("User {} left project {}", userId, projectId);
    }

    public void updateFile(UUID projectId, UUID userId, UUID fileId) {
        presence
                .computeIfAbsent(projectId, k -> new ConcurrentHashMap<>())
                .put(userId, new PresenceInfo(userId, fileId, Instant.now()));
    }

    // 🔥 heartbeat to prevent false disconnects
    public void heartbeat(UUID projectId, UUID userId) {
        presence
                .computeIfAbsent(projectId, k -> new ConcurrentHashMap<>())
                .put(userId, new PresenceInfo(userId, null, Instant.now()));
    }

    public Collection<PresenceInfo> getPresenceSnapshot(UUID projectId) {
        return presence.getOrDefault(projectId, Map.of()).values();
    }

    // ================= TTL CLEANUP =================

    public void cleanupStalePresence() {
        Instant now = Instant.now();

        for (var entry : presence.entrySet()) {
            UUID projectId = entry.getKey();
            Map<UUID, PresenceInfo> users = entry.getValue();

            users.entrySet().removeIf(e ->
                    e.getValue()
                            .lastSeen()
                            .plusSeconds(PRESENCE_TTL_SECONDS)
                            .isBefore(now)
            );

            if (users.isEmpty()) {
                presence.remove(projectId);
                connectedUsers.remove(projectId);
            }
        }
    }
}
