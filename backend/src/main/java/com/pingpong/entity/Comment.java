package com.pingpong.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "comment")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long postId;
    private Long authorId;
    private String content;
    private LocalDateTime createTime;
    private String status = "ACTIVE";

    @PrePersist
    public void prePersist() {
        if (status == null) status = "ACTIVE";
        if (createTime == null) createTime = LocalDateTime.now();
    }
}