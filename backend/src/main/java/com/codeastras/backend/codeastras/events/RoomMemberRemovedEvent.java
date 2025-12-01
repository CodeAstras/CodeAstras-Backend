package com.codeastras.backend.codeastras.events;

import com.codeastras.backend.codeastras.service.RoomServiceImpl;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class RoomMemberRemovedEvent extends ApplicationEvent {

    private final UUID roomId;
    private final UUID userId;
    private final UUID removedBy;

    public RoomMemberRemovedEvent(Object source, UUID roomId, UUID userId, UUID removedBy) {
        super(source);
        this.roomId = roomId;
        this.userId = userId;
        this.removedBy = removedBy;
    }

    public UUID getRoomId() { return roomId; }
    public UUID getUserId() { return userId; }
    public UUID getRemovedBy() { return removedBy; }
}