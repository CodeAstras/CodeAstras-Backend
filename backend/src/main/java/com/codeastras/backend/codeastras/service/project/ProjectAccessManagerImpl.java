package com.codeastras.backend.codeastras.service.project;

import com.codeastras.backend.codeastras.entity.collaborator.CollaboratorRole;
import com.codeastras.backend.codeastras.entity.collaborator.CollaboratorStatus;
import com.codeastras.backend.codeastras.entity.collaborator.ProjectCollaborator;
import com.codeastras.backend.codeastras.exception.ForbiddenException;
import com.codeastras.backend.codeastras.repository.collaborator.ProjectCollaboratorRepository;
import com.codeastras.backend.codeastras.security.ProjectAccessManager;
import com.codeastras.backend.codeastras.security.ProjectPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProjectAccessManagerImpl implements ProjectAccessManager {

    private final ProjectCollaboratorRepository collaboratorRepository;

    // GENERIC PERMISSION CHECK
    @Override
    public void require(
            UUID projectId,
            UUID userId,
            ProjectPermission permission
    ) {
        ProjectCollaborator collaborator =
                collaboratorRepository
                        .findByProjectIdAndUserId(projectId, userId)
                        .orElseThrow(() ->
                                new ForbiddenException("No access to project"));

        Set<ProjectPermission> allowed =
                permissionsForRole(collaborator.getRole());

        if (!allowed.contains(permission)) {
            throw new ForbiddenException(
                    "Permission denied: " + permission
            );
        }

        if (collaborator.getStatus() != CollaboratorStatus.ACCEPTED) {
            throw new ForbiddenException("Collaborator not accepted");
        }

    }


    // OWNER SHORTCUT
    @Override
    public void requireOwner(UUID projectId, UUID userId) {
        ProjectCollaborator collaborator =
                collaboratorRepository
                        .findByProjectIdAndUserId(projectId, userId)
                        .orElseThrow(() ->
                                new ForbiddenException("No access to project"));

        if (collaborator.getRole() != CollaboratorRole.OWNER) {
            throw new ForbiddenException("Owner permission required");
        }

        if (collaborator.getStatus() != CollaboratorStatus.ACCEPTED) {
            throw new ForbiddenException("Collaborator not accepted");
        }

    }

    // ROLE → PERMISSION MATRIX
    private Set<ProjectPermission> permissionsForRole(
            CollaboratorRole role
    ) {
        return switch (role) {

            case OWNER -> EnumSet.allOf(ProjectPermission.class);

            case COLLABORATOR -> EnumSet.of(
                    ProjectPermission.READ_TREE,
                    ProjectPermission.READ_FILE,
                    ProjectPermission.READ_COLLABORATORS,

                    ProjectPermission.CREATE_FILE,
                    ProjectPermission.CREATE_FOLDER,
                    ProjectPermission.UPDATE_FILE,

                    ProjectPermission.EXECUTE_CODE,
                    ProjectPermission.START_SESSION
            );

            case VIEWER -> EnumSet.of(
                    ProjectPermission.READ_TREE,
                    ProjectPermission.READ_FILE,
                    ProjectPermission.READ_COLLABORATORS
            );

            // FAIL CLOSED
            default -> EnumSet.noneOf(ProjectPermission.class);
        };
    }
}
