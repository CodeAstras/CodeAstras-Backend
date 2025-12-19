package com.codeastras.backend.codeastras.config;

import com.codeastras.backend.codeastras.security.JwtUtils;
import com.codeastras.backend.codeastras.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WebSocketPermissionInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final PermissionService permissionService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        StompCommand command = accessor.getCommand();
        if (command == null) return message;

        /* ================================
           1. AUTHENTICATE ON CONNECT
           ================================ */
        if (command == StompCommand.CONNECT) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new IllegalStateException("Missing Authorization header");
            }

            String token = authHeader.substring(7);
            if (!jwtUtils.validate(token)) {
                throw new IllegalStateException("Invalid JWT");
            }

            UUID userId = jwtUtils.getUserId(token);

            // 🔥 THIS FIXES principal == null 🔥
            accessor.setUser((Principal) () -> userId.toString());

            return message;
        }

        /* ================================
           2. AUTHORIZE SEND / SUBSCRIBE
           ================================ */
        if (command != StompCommand.SUBSCRIBE && command != StompCommand.SEND) {
            return message;
        }

        String destination = accessor.getDestination();
        if (destination == null) return message;

        UUID projectId = extractProjectId(destination);
        if (projectId == null) return message; // 🔥 FIXED LOGIC

        Principal principal = accessor.getUser();
        if (principal == null) {
            throw new IllegalStateException("Unauthenticated WebSocket user");
        }

        UUID userId = UUID.fromString(principal.getName());

        if (command == StompCommand.SUBSCRIBE) {
            permissionService.checkProjectReadAccess(projectId, userId);
        }

        if (command == StompCommand.SEND) {
            permissionService.checkProjectWriteAccess(projectId, userId);
        }

        return message;
    }

    private UUID extractProjectId(String destination) {
        // Example: /topic/projects/{projectId}/files/{fileId}
        String[] parts = destination.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("projects".equals(parts[i]) && i + 1 < parts.length) {
                try {
                    return UUID.fromString(parts[i + 1]);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return null;
    }
}
