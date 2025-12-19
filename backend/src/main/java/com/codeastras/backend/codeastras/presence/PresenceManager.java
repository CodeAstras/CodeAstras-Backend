package com.codeastras.backend.codeastras.presence;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PresenceManager {
    private final Map<UUID, Map<UUID, PresenceState>> projectPresence = new ConcurrentHashMap<>();

    public void userJoined(UUID projectId, UUID userId) {
        projectPresence.computeIfAbsent(projectId, k -> new ConcurrentHashMap<>())
                .put(userId, new PresenceState(userId, projectId, null, Instant.now()));
    }

    public void userLeft(UUID projectId, UUID userId) {
        Map<UUID, PresenceState> users = projectPresence.get(projectId);
        if(users != null) {
            users.remove(userId);
            if(users.isEmpty()) {
                projectPresence.remove(projectId);
            }
        }
    }

    public void updateFile(UUID projectId, UUID userId, UUID fileId) {
        Map<UUID, PresenceState> users = projectPresence.get(projectId);
        if(users != null && users.containsKey(userId)) {
            users.put(userId, new PresenceState(userId, projectId, fileId, Instant.now()));
        }
    }

    public Collection<PresenceState> getProjectPresence(UUID projectId) {
        return projectPresence.getOrDefault(projectId, Map.of()).values();
    }
}
