package com.codeastras.backend.codeastras.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InviteCollaboratorRequest {
    @NotBlank
    @Email
    private String email;
}
