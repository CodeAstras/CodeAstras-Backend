package com.codeastras.backend.codeastras.controller;

import com.codeastras.backend.codeastras.service.SessionService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/{projectId}/start")
    public String start(@PathVariable UUID projectId, Authentication auth) throws Exception {
        UUID userId = (UUID) auth.getPrincipal();
        return sessionService.startSession(projectId, userId);
    }

    @PostMapping("/{sessionId}/stop")
    public void stop(@PathVariable String sessionId) throws Exception {
        sessionService.stopSession(sessionId);
    }
}
