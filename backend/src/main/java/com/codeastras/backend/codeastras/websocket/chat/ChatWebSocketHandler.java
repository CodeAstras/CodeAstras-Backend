package com.codeastras.backend.codeastras.websocket.chat;

import com.codeastras.backend.codeastras.entity.chat.ChatMessage;
import com.codeastras.backend.codeastras.entity.chat.MessageType;
import com.codeastras.backend.codeastras.service.chat.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final ChatRoomRegistry roomRegistry;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UUID projectId = getProjectId(session);
        UUID userId = getUserId(session);
        String username = getUsername(session);

        roomRegistry.join(projectId, session);
        log.info("用户 {} ({}) 加入了聊天室 {}", username, userId, projectId);

        // Send history
        var history = chatService.getRecentMessages(projectId);
        String payload = objectMapper.writeValueAsString(Map.of(
                "type", "HISTORY",
                "messages", history));
        session.sendMessage(new TextMessage(payload));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        UUID projectId = getProjectId(session);
        UUID userId = getUserId(session);
        String username = getUsername(session);

        try {
            // Expecting simple JSON: { "content": "..." }
            Map<String, String> payload = objectMapper.readValue(message.getPayload(), Map.class);
            String content = payload.get("content");

            if (content != null && !content.isBlank()) {
                chatService.processUserMessage(projectId, userId, username, content);
            }
        } catch (Exception e) {
            log.error("Error processing message from {}", userId, e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        roomRegistry.leave(session);
    }

    private UUID getProjectId(WebSocketSession session) {
        return (UUID) session.getAttributes().get("projectId");
    }

    private UUID getUserId(WebSocketSession session) {
        return (UUID) session.getAttributes().get("userId");
    }

    private String getUsername(WebSocketSession session) {
        return (String) session.getAttributes().get("username");
    }
}
