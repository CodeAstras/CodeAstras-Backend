package com.codeastras.backend.codeastras.events;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;


public class RoomCreatedEvent extends ApplicationEvent {

    private final UUID roomId;
    private final UUID creatorId;

    public RoomCreatedEvent(Object source, UUID roomId, UUID creatorId) {
        super(source);
        this.roomId = roomId;
        this.creatorId = creatorId;
    }

    public UUID getRoomId() { return roomId; }
    public UUID getCreatorId() { return creatorId; }
}