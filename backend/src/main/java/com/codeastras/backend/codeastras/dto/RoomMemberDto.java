package com.codeastras.backend.codeastras.dto;

import java.time.Instant;
import java.util.UUID;

public class RoomMemberDto {

    private UUID userId;

    private String role;

    private Instant joinedAt;

    public RoomMemberDto() {
        this.userId = userId;
        this.role = role;
        this.joinedAt = joinedAt;
    }


    public UUID getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }
}
