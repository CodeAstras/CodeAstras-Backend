package com.codeastras.backend.codeastras.store;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CallSessionRegistry {

    // One active call per user (intentional MVP constraint)
    private final Map<UUID, UUID> userToProject = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> projectCalls = new ConcurrentHashMap<>();

    /**
     * Atomic join with capacity enforcement.
     */
    public synchronized boolean tryJoin(
            UUID projectId,
            UUID userId,
            int maxSize
    ) {
        Set<UUID> users = projectCalls
                .computeIfAbsent(projectId, k -> ConcurrentHashMap.newKeySet());

        if (users.contains(userId)) {
            return true; // idempotent
        }

        if (users.size() >= maxSize) {
            return false;
        }

        users.add(userId);
        userToProject.put(userId, projectId);
        return true;
    }

    public void leave(UUID projectId, UUID userId) {
        Set<UUID> users = projectCalls.get(projectId);
        if (users != null) {
            users.remove(userId);
            if (users.isEmpty()) {
                projectCalls.remove(projectId);
            }
        }
        userToProject.remove(userId);
    }

    public boolean isInCall(UUID projectId, UUID userId) {
        Set<UUID> users = projectCalls.get(projectId);
        return users != null && users.contains(userId);
    }

    public int size(UUID projectId) {
        Set<UUID> users = projectCalls.get(projectId);
        return users == null ? 0 : users.size();
    }

    public UUID getProjectForUser(UUID userId) {
        return userToProject.get(userId);
    }

    /**
     * Defensive copy — callers never mutate internal state.
     */
    public Set<UUID> getParticipants(UUID projectId) {
        return Set.copyOf(projectCalls.getOrDefault(projectId, Set.of()));
    }
}
