package com.codeastras.backend.codeastras.dto.profile;

import jakarta.validation.constraints.NotBlank;

public class UpdateAvatarRequest {

    @NotBlank
    private String avatarUrl;

    public String getAvatarUrl() {
        return avatarUrl;
    }
}

