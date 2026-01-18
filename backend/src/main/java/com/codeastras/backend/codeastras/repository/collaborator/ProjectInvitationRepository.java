package com.codeastras.backend.codeastras.repository.collaborator;

import com.codeastras.backend.codeastras.entity.collaborator.InvitationStatus;
import com.codeastras.backend.codeastras.entity.collaborator.ProjectInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, UUID> {

    Optional<ProjectInvitation> findByProjectIdAndInviteeId(UUID projectId, UUID inviteeId);

    List<ProjectInvitation> findAllByInviteeIdAndStatus(UUID inviteeId, InvitationStatus status);

    List<ProjectInvitation> findAllByProjectIdAndStatus(UUID projectId, InvitationStatus status);
}
