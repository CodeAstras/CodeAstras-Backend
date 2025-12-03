package com.codeastras.backend.codeastras.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "rtc_signals")
public class RtcSignal {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Column(name = "signal_type", nullable = false)
    private String signalType;  // OFFER, ANSWER, ICE

    @Column(columnDefinition = "jsonb")
    private String payload;

    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
}

