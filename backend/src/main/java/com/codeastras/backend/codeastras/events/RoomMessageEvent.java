package com.codeastras.backend.codeastras.events;

import com.codeastras.backend.codeastras.dto.RoomMessageDto;
import com.codeastras.backend.codeastras.service.RoomServiceImpl;
import org.springframework.context.ApplicationEvent;

public class RoomMessageEvent extends ApplicationEvent {

    private final RoomMessageDto message;

    public RoomMessageEvent(Object source, RoomMessageDto message) {
        super(source);
        this.message = message;
    }

    public RoomMessageDto getMessage() { return message; }
}
