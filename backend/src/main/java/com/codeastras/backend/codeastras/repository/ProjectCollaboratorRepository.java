package com.codeastras.backend.codeastras.repository;

import com.codeastras.backend.codeastras.entity.CollaboratorRole;
import com.codeastras.backend.codeastras.entity.CollaboratorStatus;
import com.codeastras.backend.codeastras.entity.ProjectCollaborator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectCollaboratorRepository extends JpaRepository<ProjectCollaborator, UUID> {
    Optional<ProjectCollaborator> findByProjectIdAndUserId(UUID projectId, UUID userId);
    boolean existsByProjectIdAndUserIdAndStatus(UUID projectId, UUID userId, CollaboratorStatus status);
    List<ProjectCollaborator> findAllByUserIdAndStatus(UUID userId, CollaboratorStatus status);
    List<ProjectCollaborator> findAllByProjectIdAndStatus(UUID projectId, CollaboratorStatus status);
    List<ProjectCollaborator> findAllByProjectId(UUID projectId);
    List<ProjectCollaborator> findAllByUserId(UUID userId);
}
