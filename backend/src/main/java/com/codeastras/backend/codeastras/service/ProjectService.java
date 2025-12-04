package com.codeastras.backend.codeastras.service;

import com.codeastras.backend.codeastras.dto.CreateProjectRequest;
import com.codeastras.backend.codeastras.dto.ProjectResponse;
import com.codeastras.backend.codeastras.entity.Project;

import java.util.List;
import java.util.UUID;

public interface ProjectService {
    ProjectResponse createProject(CreateProjectRequest request, UUID ownerId);
    ProjectResponse getProject(UUID projectId, UUID requesterId);
    List<ProjectResponse> getProjectsForUser(UUID ownerId);
}

