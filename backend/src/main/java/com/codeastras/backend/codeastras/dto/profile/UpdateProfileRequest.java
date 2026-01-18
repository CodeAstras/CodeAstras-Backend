package com.codeastras.backend.codeastras.dto.profile;

import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @Size(max = 100)
    private String displayName;

    @Size(max = 160)
    private String bio;

    @Size(max = 100)
    private String location;

    public String getDisplayName() {
        return displayName;
    }

    public String getBio() {
        return bio;
    }

    public String getLocation() {
        return location;
    }
}
