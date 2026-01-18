package com.codeastras.backend.codeastras.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@AllArgsConstructor
@Getter
@Setter
public class PublicProfileResponse {

    private String username;
    private String displayName;
    private String bio;
    private String location;
    private String avatarUrl;
    private Instant joinedAt;
}
