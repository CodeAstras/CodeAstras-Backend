package com.codeastras.backend.codeastras.dto;

import java.util.UUID;
import java.time.Instant;
import java.util.List;

public class RoomResponse {
    private UUID id;
    private String name;
    private UUID createdBy;
    private Instant createdAt;
    private boolean isActive;
    private List<RoomMemberDto> members;

    public RoomResponse() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean active) {
        isActive = active;
    }

    public List<RoomMemberDto> getMembers() {
        return members;
    }

    public void setMembers(List<RoomMemberDto> members) {
        this.members = members;
    }
}
