package com.codeastras.backend.codeastras.config;

import com.codeastras.backend.codeastras.security.JwtUtils;
import com.codeastras.backend.codeastras.security.ProjectAccessManager;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WebSocketPermissionInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final ProjectAccessManager accessManager;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

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
            Instant expiresAt = jwtUtils.getExpiry(token); // 🔥 NEW

            accessor.setUser((Principal) () -> userId.toString());

            // 🔥 Attach expiry to session
            accessor.getSessionAttributes()
                    .put("jwt_expiry", expiresAt);

            return message;
        }

        if (command == StompCommand.SEND || command == StompCommand.SUBSCRIBE) {

            Instant expiry = (Instant) accessor
                    .getSessionAttributes()
                    .get("jwt_expiry");

            if (expiry != null && expiry.isBefore(Instant.now())) {
                throw new IllegalStateException("WebSocket token expired");
            }
        }

        // AUTHORIZE SEND / SUBSCRIBE
        if (command != StompCommand.SUBSCRIBE &&
                command != StompCommand.SEND) {
            return message;
        }

        String destination = accessor.getDestination();
        if (destination == null) {
            return message;
        }

        UUID projectId = extractProjectId(destination);
        if (projectId == null) {
            // Not project-scoped (e.g. /topic/errors)
            return message;
        }

        Principal principal = accessor.getUser();
        if (principal == null) {
            throw new IllegalStateException("Unauthenticated WebSocket user");
        }

        UUID userId = UUID.fromString(principal.getName());

        if (command == StompCommand.SUBSCRIBE) {
            accessManager.requireRead(projectId, userId);
        }

        if (command == StompCommand.SEND) {
            accessManager.requireWrite(projectId, userId);
        }

        return message;
    }

    // Helpers
    private UUID extractProjectId(String destination) {

        // /topic/projects/{projectId}/code
        // /app/projects/{projectId}/edit

        String[] parts = destination.split("/");

        for (int i = 0; i < parts.length; i++) {
            if ("projects".equals(parts[i]) && i + 1 < parts.length) {
                try {
                    return UUID.fromString(parts[i + 1]);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return null;
    }
}
