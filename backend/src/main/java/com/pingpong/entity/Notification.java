package com.pingpong.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long receiverId;
    private String type;
    private String title;
    private String content;
    private Boolean isRead = false;
    private Long relatedBusinessId;
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (isRead == null) isRead = false;
        if (createTime == null) createTime = LocalDateTime.now();
    }
}