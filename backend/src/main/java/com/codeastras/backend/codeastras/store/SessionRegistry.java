package com.codeastras.backend.codeastras.store;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {

    public static class SessionInfo {
        public final String sessionId;
        public final String containerName;
        public final UUID projectId;
        public final UUID userId;

        public SessionInfo(String sessionId, String containerName, UUID projectId, UUID userId) {
            this.sessionId = sessionId;
            this.containerName = containerName;
            this.projectId = projectId;
            this.userId = userId;
        }
    }

    // sessionId -> session info
    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();

    // projectId -> sessionId
    private final Map<UUID, String> projectToSession = new ConcurrentHashMap<>();

    // Register a session
    public void register(SessionInfo info) {
        sessions.put(info.sessionId, info);
        projectToSession.put(info.projectId, info.sessionId);
    }

    // Get session info by sessionId
    public Optional<SessionInfo> get(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    // Get sessionId using projectId
    public Optional<String> getSessionIdForProject(UUID projectId) {
        return Optional.ofNullable(projectToSession.get(projectId));
    }

    // 🔥 For your FileService compatibility
    public String getSessionIdByProject(UUID projectId) {
        return projectToSession.get(projectId);
    }

    // Clean-up session
    public void remove(String sessionId) {
        SessionInfo info = sessions.remove(sessionId);
        if (info != null) {
            projectToSession.remove(info.projectId);
        }
    }

    public SessionInfo getByProject(UUID projectId) {
        return sessions.values()
                .stream()
                .filter(s -> s.projectId.equals(projectId))
                .findFirst()
                .orElse(null);
    }

}
