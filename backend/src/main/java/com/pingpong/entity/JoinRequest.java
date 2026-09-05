package com.pingpong.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "join_request")
public class JoinRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long clubId;
    private Long applicantId;
    private String status = "PENDING";
    private LocalDateTime applyTime;
    private LocalDateTime approveTime;
    private Long approverId;
    private String rejectReason;

    @PrePersist
    public void prePersist() {
        if (status == null) status = "PENDING";
        if (applyTime == null) applyTime = LocalDateTime.now();
    }
}