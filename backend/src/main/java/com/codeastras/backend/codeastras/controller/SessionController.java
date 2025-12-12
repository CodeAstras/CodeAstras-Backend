package com.codeastras.backend.codeastras.controller;

import com.codeastras.backend.codeastras.entity.Project;
import com.codeastras.backend.codeastras.exception.ForbiddenException;
import com.codeastras.backend.codeastras.exception.ResourceNotFoundException;
import com.codeastras.backend.codeastras.repository.ProjectRepository;
import com.codeastras.backend.codeastras.service.SessionService;
import com.codeastras.backend.codeastras.store.SessionRegistry;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    private final SessionService sessionService;
    private final ProjectRepository projectRepo;
    private final SessionRegistry sessionRegistry;

    public SessionController(SessionService sessionService,
                             ProjectRepository projectRepo,
                             SessionRegistry sessionRegistry) {
        this.sessionService = sessionService;
        this.projectRepo = projectRepo;
        this.sessionRegistry = sessionRegistry;
    }

    @PostMapping("/{projectId}/start")
    public String start(@PathVariable UUID projectId, Authentication auth) throws Exception {

        UUID userId = (UUID) auth.getPrincipal();

        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (!project.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("You cannot start a session for this project");
        }

        return sessionService.startSession(projectId, userId);
    }

    @PostMapping("/{sessionId}/stop")
    public void stop(@PathVariable String sessionId, Authentication auth) throws Exception {

        UUID userId = (UUID) auth.getPrincipal();

        sessionService.stopSession(sessionId, userId);
    }
}
