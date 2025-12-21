package com.codeastras.backend.codeastras.config;

import com.codeastras.backend.codeastras.security.AuthUtil;
import com.codeastras.backend.codeastras.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class PresenceWebSocketController {

    private final PresenceService presenceService;

    // ---------------- JOIN ----------------

    @MessageMapping("/projects/{projectId}/presence/join")
    public void join(
            @DestinationVariable UUID projectId,
            Principal principal
    ) {
        UUID userId = AuthUtil.requireUserId(principal);
        presenceService.join(projectId, userId);
    }

    // ---------------- LEAVE (EXPLICIT UI INTENT) ----------------

    @MessageMapping("/projects/{projectId}/presence/leave")
    public void leaveExplicit(
            @DestinationVariable UUID projectId,
            Principal principal
    ) {
        UUID userId = AuthUtil.requireUserId(principal);
        presenceService.leaveExplicit(projectId, userId);
    }

    // ---------------- FILE CHANGE ----------------

    @MessageMapping("/projects/{projectId}/presence/file")
    public void changeFile(
            @DestinationVariable UUID projectId,
            UUID fileId,
            Principal principal
    ) {
        UUID userId = AuthUtil.requireUserId(principal);
        presenceService.changeFile(projectId, userId, fileId);
    }

    // ---------------- HEARTBEAT ----------------

    @MessageMapping("/projects/{projectId}/presence/heartbeat")
    public void heartbeat(
            @DestinationVariable UUID projectId,
            Principal principal
    ) {
        UUID userId = AuthUtil.requireUserId(principal);
        presenceService.heartbeat(projectId, userId);
    }

    // ---------------- SYNC ----------------

    @MessageMapping("/projects/{projectId}/presence/sync")
    public void sync(
            @DestinationVariable UUID projectId,
            Principal principal
    ) {
        UUID userId = AuthUtil.requireUserId(principal);
        presenceService.sync(projectId, userId);
    }
}
