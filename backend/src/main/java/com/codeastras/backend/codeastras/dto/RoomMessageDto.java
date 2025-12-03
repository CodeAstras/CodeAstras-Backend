package com.codeastras.backend.codeastras.dto;

import java.time.Instant;
import java.util.UUID;

public class RoomMessageDto
{
    private UUID id;
    private UUID roomId;
    private UUID userId;
    private String content;
    private String type;
    private Instant timestamp;

    public RoomMessageDto() {
        this.id = id;
        this.roomId = roomId;
        this.userId = userId;
        this.content = content;
        this.type = type;
        this.timestamp = timestamp;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
