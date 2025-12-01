package com.codeastras.backend.codeastras.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "room_activity")
public class RoomActivity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 50)
    private String action;   // JOIN, LEAVE, SHARE_SCREEN etc

    @Column(columnDefinition = "jsonb")
    private String metadata;

    private Timestamp occurredAt = new Timestamp(System.currentTimeMillis());
}

