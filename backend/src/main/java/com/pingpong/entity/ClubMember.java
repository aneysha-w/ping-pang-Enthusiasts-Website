package com.pingpong.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "club_member", uniqueConstraints = @UniqueConstraint(columnNames = {"clubId", "userId"}))
public class ClubMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long clubId;
    private Long userId;
    private String role = "MEMBER";
    private LocalDateTime joinTime;
    private String status = "ACTIVE";

    @PrePersist
    public void prePersist() {
        if (role == null) role = "MEMBER";
        if (status == null) status = "ACTIVE";
        if (joinTime == null) joinTime = LocalDateTime.now();
    }
}