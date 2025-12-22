package com.codeastras.backend.codeastras.dto;

import com.codeastras.backend.codeastras.entity.CollaboratorRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCollaboratorRoleRequest {

    @NotNull
    private CollaboratorRole role;
}
