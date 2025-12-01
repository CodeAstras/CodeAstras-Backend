package com.codeastras.backend.codeastras.events;

import com.codeastras.backend.codeastras.service.RoomServiceImpl;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class RoomMemberAddedEvent extends ApplicationEvent {

    private final UUID roomId;
    private final UUID newMemberId;
    private final UUID addedBy;

    public RoomMemberAddedEvent(Object source, UUID roomId, UUID newMemberId, UUID addedBy) {
        super(source);
        this.roomId = roomId;
        this.newMemberId = newMemberId;
        this.addedBy = addedBy;
    }

    public UUID getRoomId() { return roomId; }
    public UUID getNewMemberId() { return newMemberId; }
    public UUID getAddedBy() { return addedBy; }
}