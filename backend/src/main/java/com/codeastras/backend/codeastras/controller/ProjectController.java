package com.codeastras.backend.codeastras.controller;

import com.codeastras.backend.codeastras.dto.CreateProjectRequest;
import com.codeastras.backend.codeastras.dto.ProjectResponse;
import com.codeastras.backend.codeastras.entity.Project;
import com.codeastras.backend.codeastras.security.AuthUtil;
import com.codeastras.backend.codeastras.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication
    ) {
        UUID userId = AuthUtil.requireUserId(authentication);

        ProjectResponse response = projectService.createProject(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(value = "/{projectId}", produces = "application/json")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable UUID projectId,
            Authentication authentication
    ) {
        UUID userId = AuthUtil.requireUserId(authentication);
        ProjectResponse response = projectService.getProject(projectId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<ProjectResponse>> getMyProjects(Authentication auth) {

        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(List.of());
        }

        UUID userId = AuthUtil.requireUserId(auth);

        List<ProjectResponse> projects =
                projectService.getProjectsForUser(userId);

        return ResponseEntity.ok(projects);
    }

}
