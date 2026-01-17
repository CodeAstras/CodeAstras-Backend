package com.codeastras.backend.codeastras.dto.chat;

import com.codeastras.backend.codeastras.entity.chat.MessageType;
import lombok.Data;

import java.util.UUID;

@Data
public class CallSignalMessage {
    private MessageType type;
    private UUID projectId;
    private UUID fromUserId;
    private UUID toUserId;
}
