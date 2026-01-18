package com.codeastras.backend.codeastras.websocket.chat;

import com.codeastras.backend.codeastras.security.JwtUtils;
import com.codeastras.backend.codeastras.security.ProjectAccessManager;
import com.codeastras.backend.codeastras.security.ProjectPermission;
import com.codeastras.backend.codeastras.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtils jwtUtils;
    private final ProjectAccessManager accessManager;
    private final UserRepository userRepository;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        // URI: /ws/chat/{projectId}?token=...
        URI uri = request.getURI();
        String path = uri.getPath();
        String query = uri.getQuery();

        // Extract Project ID
        UUID projectId = extractProjectId(path);
        if (projectId == null) {
            log.warn("Chat handshake failed: No project ID in path {}", path);
            return false;
        }

        // Extract Token
        String token = extractToken(query);
        if (token == null) {
            // Fallback: Check headers if client supports custom headers (unlikely for
            // browser WebSocket)
            // But we can check for protocol or cookie if needed. For now strict query
            // param.
            log.warn("Chat handshake failed: No token provided");
            return false;
        }

        try {
            // Validate Token
            if (!jwtUtils.validate(token)) {
                log.warn("Chat handshake failed: Invalid token");
                return false;
            }

            UUID userId = jwtUtils.getUserIdFromToken(token);

            // 🔐 Authorization Check
            accessManager.require(projectId, userId, ProjectPermission.READ_PROJECT);

            // Fetch Username (for chat display)
            String username = userRepository.findById(userId)
                    .map(u -> u.getUsername()) // Assuming getUsername() exists
                    .orElse("Unknown");

            // Store attributes for the session
            attributes.put("projectId", projectId);
            attributes.put("userId", userId);
            attributes.put("username", username);

            return true;
        } catch (Exception e) {
            log.error("Chat handshake error", e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Exception exception) {
        // No-op
    }

    private UUID extractProjectId(String path) {
        try {
            // Expected: /ws/chat/<UUID>
            String[] parts = path.split("/");
            // parts[0] = "", parts[1] = "ws", parts[2] = "chat", parts[3] = UUID
            if (parts.length >= 4 && "chat".equals(parts[2])) {
                return UUID.fromString(parts[parts.length - 1]);
            }
        } catch (Exception e) {
            return null;
        }
        return null; // fallback
    }

    private String extractToken(String query) {
        if (query == null)
            return null;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2 && "token".equals(pair[0])) {
                return pair[1];
            }
        }
        return null;
    }
}
