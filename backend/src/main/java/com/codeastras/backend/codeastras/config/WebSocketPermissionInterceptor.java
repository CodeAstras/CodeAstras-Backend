package com.codeastras.backend.codeastras.config;

import com.codeastras.backend.codeastras.security.JwtUtils;
import com.codeastras.backend.codeastras.security.ProjectAccessManager;
import com.codeastras.backend.codeastras.security.ProjectPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
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

        // ================= CONNECT =================
        if (command == StompCommand.CONNECT) {

            String authHeader =
                    accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new IllegalStateException("Missing Authorization header");
            }

            String token = authHeader.substring(7);

            // 🔐 STRICT: ACCESS TOKENS ONLY
            jwtUtils.validateAccessToken(token);

            UUID userId = jwtUtils.getUserIdFromToken(token);
            Instant expiresAt = jwtUtils.getExpiry(token);

            accessor.setUser((Principal) userId::toString);
            accessor.getSessionAttributes()
                    .put("jwt_expiry", expiresAt);

            return message;
        }

        // ================= EXPIRY =================
        if (command == StompCommand.SEND ||
                command == StompCommand.SUBSCRIBE) {

            Instant expiry = (Instant)
                    accessor.getSessionAttributes()
                            .get("jwt_expiry");

            if (expiry != null && expiry.isBefore(Instant.now())) {
                throw new IllegalStateException("WebSocket token expired");
            }
        }

        // ================= AUTHZ =================
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
            return message; // non-project topic
        }

        Principal principal = accessor.getUser();
        if (principal == null) {
            throw new IllegalStateException("Unauthenticated WebSocket user");
        }

        UUID userId = UUID.fromString(principal.getName());

        // ---------------- SUBSCRIBE ----------------
        if (command == StompCommand.SUBSCRIBE) {

            if (destination.contains("/presence") ||
                    destination.contains("/run-output") ||
                    destination.contains("/errors")) {

                accessManager.require(
                        projectId,
                        userId,
                        ProjectPermission.READ_FILE
                );
            }

            if (destination.contains("/tree")) {
                accessManager.require(
                        projectId,
                        userId,
                        ProjectPermission.READ_TREE
                );
            }

            return message;
        }

        // ---------------- SEND ----------------
        if (destination.contains("/edit")) {

            accessManager.require(
                    projectId,
                    userId,
                    ProjectPermission.UPDATE_FILE
            );

        } else if (destination.contains("/run")) {

            accessManager.require(
                    projectId,
                    userId,
                    ProjectPermission.EXECUTE_CODE
            );

        } else if (destination.contains("/session/start")) {

            accessManager.require(
                    projectId,
                    userId,
                    ProjectPermission.START_SESSION
            );

        } else if (destination.contains("/session/stop")) {

            accessManager.require(
                    projectId,
                    userId,
                    ProjectPermission.STOP_SESSION
            );
        }

        return message;
    }

    // ==================================================

    private UUID extractProjectId(String destination) {

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
