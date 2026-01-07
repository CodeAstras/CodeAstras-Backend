package com.codeastras.backend.codeastras.security;

import java.util.UUID;


public interface ProjectAccessManager {

    void require(
            UUID projectId,
            UUID userId,
            ProjectPermission permission
    );

    default void requireOwner(UUID projectId, UUID userId) {
        require(projectId, userId, ProjectPermission.OWNER_ONLY);
    }
}
