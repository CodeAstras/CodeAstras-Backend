package com.codeastras.backend.codeastras.websocket.chat;

import com.codeastras.backend.codeastras.entity.chat.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomRegistry {

    private final ObjectMapper objectMapper;

    // projectId -> Set<Session>
    private final Map<UUID, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    // sessionId -> projectId (for quick lookup on disconnect)
    private final Map<String, UUID> sessionProjectMap = new ConcurrentHashMap<>();

    public void join(UUID projectId, WebSocketSession session) {
        rooms.computeIfAbsent(projectId, k -> new CopyOnWriteArraySet<>()).add(session);
        sessionProjectMap.put(session.getId(), projectId);
    }

    public void leave(WebSocketSession session) {
        UUID projectId = sessionProjectMap.remove(session.getId());
        if (projectId != null) {
            Set<WebSocketSession> sessions = rooms.get(projectId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    rooms.remove(projectId);
                }
            }
        }
    }

    public void broadcast(UUID projectId, ChatMessage message) {
        Set<WebSocketSession> sessions = rooms.get(projectId);
        if (sessions == null || sessions.isEmpty())
            return;

        try {
            String payload = objectMapper.writeValueAsString(message);
            TextMessage textMessage = new TextMessage(payload);

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(textMessage);
                    } catch (IOException e) {
                        log.warn("Failed to send message to session {}", session.getId(), e);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Failed to serialize chat message", e);
        }
    }
}
