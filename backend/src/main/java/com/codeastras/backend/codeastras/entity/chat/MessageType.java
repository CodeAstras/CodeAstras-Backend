package com.codeastras.backend.codeastras.entity.chat;

public enum MessageType {
    TEXT,
    FILE,
    SYSTEM,

    CALL_JOIN,
    CALL_LEAVE,
    CALL_OFFER,
    CALL_ANSWER,
    CALL_ICE
}
