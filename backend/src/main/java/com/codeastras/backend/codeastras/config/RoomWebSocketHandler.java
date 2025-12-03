package com.codeastras.backend.codeastras.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.handler.TextWebSocketHandler;



import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomWebSocketHandler extends TextWebSocketHandler {

    // Map<roomId, Map<userId, WebSocketSession>>
    private final Map<String, Map<String, WebSocketSession>> rooms = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // roomId is expected in URL: /ws/signal/{roomId}
        URI uri = session.getUri();
        if (uri == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        String path = uri.getPath();
        String[] parts = path.split("/");
        String roomId = parts.length > 3 ? parts[3] : null; // ["", "ws", "signal", "{roomId}"]
        if (roomId == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        // fetch authenticated user id from attributes (set by JwtHandshakeInterceptor)
        var attrs = session.getAttributes();
        String userId = (String) attrs.get("userId");
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Unauthenticated"));
            return;
        }

        rooms.computeIfAbsent(roomId, rid -> new ConcurrentHashMap<>()).put(userId, session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Expect JSON payload like:
        // { "type":"offer|answer|ice", "to":"<targetUserId>", "from":"<senderUserId>", "payload":{...} }
        var payload = message.getPayload();
        // lightweight parsing - use your preferred JSON lib (Jackson recommended)
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var node = mapper.readTree(payload);
        String to = node.has("to") ? node.get("to").asText() : null;
        String roomId = extractRoomId(session);

        if (roomId == null) {
            session.close(CloseStatus.BAD_DATA.withReason("Missing roomId"));
            return;
        }

        if (to != null) {
            // direct signaling to specific peer
            var roomMap = rooms.get(roomId);
            if (roomMap != null && roomMap.containsKey(to)) {
                WebSocketSession receiver = roomMap.get(to);
                // forward message as-is (or you can sanitize)
                receiver.sendMessage(new TextMessage(payload));
            } else {
                // optionally buffer the signal to rtc_signals table if receiver offline
                // TODO: integrate rtc_signals persistence
            }
        } else {
            // broadcast to all participants in room except sender
            var roomMap = rooms.get(roomId);
            if (roomMap != null) {
                String senderId = (String) session.getAttributes().get("userId");
                for (var entry : roomMap.entrySet()) {
                    if (!entry.getKey().equals(senderId)) {
                        try { entry.getValue().sendMessage(new TextMessage(payload)); } catch (Exception ex) { /* log */ }
                    }
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String roomId = extractRoomId(session);
        if (roomId == null) return;
        String userId = (String) session.getAttributes().get("userId");
        var roomMap = rooms.get(roomId);
        if (roomMap != null && userId != null) {
            roomMap.remove(userId);
            if (roomMap.isEmpty()) rooms.remove(roomId);
        }
        // Optionally publish a RoomMemberRemovedEvent or presence event via ApplicationEventPublisher
    }

    private String extractRoomId(WebSocketSession session) {
        var uri = session.getUri();
        if (uri == null) return null;
        String[] parts = uri.getPath().split("/");
        return parts.length > 3 ? parts[3] : null;
    }
}