package com.pingpong.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "enrollment", uniqueConstraints = @UniqueConstraint(columnNames = {"eventId", "userId"}))
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long eventId;
    private Long userId;
    private LocalDateTime enrollTime;
    private String status = "ENROLLED";

    @PrePersist
    public void prePersist() {
        if (enrollTime == null) enrollTime = LocalDateTime.now();
        if (status == null) status = "ENROLLED";
    }
}