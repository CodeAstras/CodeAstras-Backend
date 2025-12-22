package com.codeastras.backend.codeastras.controller;

import com.codeastras.backend.codeastras.security.AuthUtil;
import com.codeastras.backend.codeastras.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    private static final Logger LOG =
            LoggerFactory.getLogger(SessionController.class);

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    // ------------------------------------------------
    // START SESSION
    // ------------------------------------------------
    @PostMapping("/{projectId}/start")
    public ResponseEntity<Map<String, String>> start(
            @PathVariable UUID projectId,
            Authentication auth
    ) throws Exception {

        UUID userId = AuthUtil.requireUserId(auth);

        String sessionId = sessionService.startSession(projectId, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("sessionId", sessionId));
    }


    // ------------------------------------------------
    // STOP SESSION
    // ------------------------------------------------
    @PostMapping("/{sessionId}/stop")
    public ResponseEntity<Void> stop(
            @PathVariable String sessionId,
            Authentication auth
    ) throws Exception {

        UUID userId = AuthUtil.requireUserId(auth);

        LOG.info("User {} requested stopSession {}", userId, sessionId);

        sessionService.stopSession(sessionId, userId);
        return ResponseEntity.noContent().build();
    }
}
