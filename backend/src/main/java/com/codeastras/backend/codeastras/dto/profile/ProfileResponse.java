package com.codeastras.backend.codeastras.dto.profile;

public class ProfileResponse extends PublicProfileResponse{

    private String email;

    public ProfileResponse(
            String username,
            String displayName,
            String bio,
            String location,
            String avatarUrl,
            java.time.Instant joinedAt,
            String email
    ) {
        super(username, displayName, bio, location, avatarUrl, joinedAt);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
