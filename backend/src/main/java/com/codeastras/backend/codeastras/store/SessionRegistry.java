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

    private static final Logger log =
            LoggerFactory.getLogger(SessionRegistry.class);

    private static final long PRESENCE_TTL_SECONDS = 30;

    // ================= SESSION STATE =================

    // sessionId -> SessionInfo
    private final Map<String, SessionInfo> bySessionId =
            new ConcurrentHashMap<>();

    // projectId -> SessionInfo
    private final Map<UUID, SessionInfo> byProjectId =
            new ConcurrentHashMap<>();

    // ================= WEBSOCKET MAPPINGS =================

    // wsSessionId -> userId
    private final Map<String, UUID> wsToUser =
            new ConcurrentHashMap<>();

    // wsSessionId -> projectId
    private final Map<String, UUID> wsToProject =
            new ConcurrentHashMap<>();

    // ================= PRESENCE =================

    // projectId -> (userId -> presence)
    private final Map<UUID, Map<UUID, PresenceInfo>> presence =
            new ConcurrentHashMap<>();

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

        SessionInfo old = byProjectId.get(projectId);
        if (old != null && !old.getSessionId().equals(sessionId)) {
            bySessionId.remove(old.getSessionId());
        }

        bySessionId.put(sessionId, info);
        byProjectId.put(projectId, info);

        log.info("✅ Registered session {} for project {}", sessionId, projectId);
    }

    public void remove(String sessionId) {
        SessionInfo info = bySessionId.remove(sessionId);
        if (info != null) {
            byProjectId.remove(info.getProjectId());
            presence.remove(info.getProjectId());
            log.info("🗑 Removed session {}", sessionId);
        }
    }

    // ================= LOOKUPS =================

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

    // ================= WEBSOCKET LIFECYCLE =================

    public void wsUserJoined(
            String wsSessionId,
            UUID projectId,
            UUID userId
    ) {
        if (wsSessionId == null || projectId == null || userId == null) {
            return;
        }

        wsToUser.put(wsSessionId, userId);
        wsToProject.put(wsSessionId, projectId);

        userJoined(projectId, userId);

        log.debug("🟢 WS {} joined project {}", wsSessionId, projectId);
    }

    public void wsUserLeft(String wsSessionId) {

        if (wsSessionId == null) return;

        UUID projectId = wsToProject.remove(wsSessionId);
        UUID userId = wsToUser.remove(wsSessionId);

        if (projectId != null && userId != null) {
            userLeft(projectId, userId);
            log.debug("🔴 WS {} left project {}", wsSessionId, projectId);
        }
    }

    // ================= PRESENCE =================

    /**
     * @return true if this was a NEW join
     */
    public boolean userJoined(UUID projectId, UUID userId) {

        presence.computeIfAbsent(
                projectId,
                k -> new ConcurrentHashMap<>()
        );

        boolean isNew =
                !presence.get(projectId).containsKey(userId);

        presence.get(projectId)
                .put(userId, new PresenceInfo(userId, null, Instant.now()));

        return isNew;
    }

    /**
     * @return true if user was present and removed
     */
    public boolean userLeft(UUID projectId, UUID userId) {

        Map<UUID, PresenceInfo> users = presence.get(projectId);
        if (users == null) return false;

        boolean removed = users.remove(userId) != null;

        if (users.isEmpty()) {
            presence.remove(projectId);
        }

        return removed;
    }

    /**
     * @return true if active file changed
     */
    public boolean updateFile(UUID projectId, UUID userId, UUID fileId) {

        presence.computeIfAbsent(
                projectId,
                k -> new ConcurrentHashMap<>()
        );

        PresenceInfo prev = presence.get(projectId).get(userId);

        boolean changed =
                prev == null || !Objects.equals(prev.fileId(), fileId);

        presence.get(projectId)
                .put(userId, new PresenceInfo(userId, fileId, Instant.now()));

        return changed;
    }

    public void heartbeat(UUID projectId, UUID userId) {

        presence.computeIfAbsent(
                projectId,
                k -> new ConcurrentHashMap<>()
        );

        presence.get(projectId)
                .put(userId, new PresenceInfo(userId, null, Instant.now()));
    }

    public Collection<PresenceInfo> getPresenceSnapshot(UUID projectId) {
        return presence
                .getOrDefault(projectId, Map.of())
                .values();
    }

    /**
     * 🔥 HARD CLEANUP — user logs out / token revoked
     */
    public void userLeftEverywhere(UUID userId) {

        for (UUID projectId : new HashSet<>(presence.keySet())) {

            Map<UUID, PresenceInfo> users = presence.get(projectId);
            if (users == null) continue;

            users.remove(userId);

            if (users.isEmpty()) {
                presence.remove(projectId);
            }
        }

        log.debug("User {} removed from all projects", userId);
    }

    // ================= TTL CLEANUP =================

    public void cleanupStalePresence() {

        Instant now = Instant.now();

        for (UUID projectId : new HashSet<>(presence.keySet())) {

            Map<UUID, PresenceInfo> users = presence.get(projectId);
            if (users == null) continue;

            users.entrySet().removeIf(e ->
                    e.getValue()
                            .lastSeen()
                            .plusSeconds(PRESENCE_TTL_SECONDS)
                            .isBefore(now)
            );

            if (users.isEmpty()) {
                presence.remove(projectId);
            }
        }
    }
}
