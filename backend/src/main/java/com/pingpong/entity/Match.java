package com.pingpong.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "match")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long eventId;
    private Integer round;
    private Long playerA;
    private Long playerB;
    private Long winnerId;
    private String status = "PENDING";
    private Boolean isBye = false;

    public Match() {}
    public Match(Long eventId, Integer round, Long playerA, Long playerB) {
        this.eventId = eventId;
        this.round = round;
        this.playerA = playerA;
        this.playerB = playerB;
    }
}