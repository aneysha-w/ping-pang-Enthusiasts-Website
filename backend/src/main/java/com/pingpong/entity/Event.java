package com.pingpong.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "event")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String level;
    private String format;
    private Integer maxPlayers;
    private Integer enrolledCount = 0;
    private LocalDateTime startTime;
    private LocalDateTime enrollDeadline;
    private String location;
    private String status = "PENDING";
    private Long organizerId;
    private Long clubId;

    @PrePersist
    public void prePersist() {
        if (enrolledCount == null) enrolledCount = 0;
        if (status == null) status = "PENDING";
    }
}