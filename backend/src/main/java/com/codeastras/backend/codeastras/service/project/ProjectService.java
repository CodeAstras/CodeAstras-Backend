package com.codeastras.backend.codeastras.service.project;

import com.codeastras.backend.codeastras.dto.project.CreateProjectRequest;
import com.codeastras.backend.codeastras.dto.project.ProjectResponse;

import java.util.List;
import java.util.UUID;

public interface ProjectService {
    ProjectResponse createProject(CreateProjectRequest request, UUID ownerId);
    ProjectResponse getProject(UUID projectId, UUID requesterId);
    List<ProjectResponse> getProjectsForUser(UUID ownerId);
}

