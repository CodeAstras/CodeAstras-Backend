package com.codeastras.backend.codeastras.entity.collaborator;

import com.codeastras.backend.codeastras.entity.project.Project;
import com.codeastras.backend.codeastras.entity.auth.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"project_id", "invitee_id"}
        )
)
@Getter
@Setter
public class ProjectInvitation {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private Project project;

    @ManyToOne(optional = false)
    private User inviter;

    @ManyToOne(optional = false)
    private User invitee;

    @Enumerated(EnumType.STRING)
    private CollaboratorRole role;

    @Enumerated(EnumType.STRING)
    private InvitationStatus status;

    private Instant createdAt;

    private Instant respondedAt;
}
