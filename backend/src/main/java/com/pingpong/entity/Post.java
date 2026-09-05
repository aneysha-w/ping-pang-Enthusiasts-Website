package com.pingpong.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long clubId;
    private Long authorId;
    private String title;
    private String content;
    private Integer commentCount = 0;
    private LocalDateTime publishTime;
    private LocalDateTime editTime;
    private String status = "ACTIVE";
    private Boolean formerMemberPost = false;

    @PrePersist
    public void prePersist() {
        if (commentCount == null) commentCount = 0;
        if (status == null) status = "ACTIVE";
        if (formerMemberPost == null) formerMemberPost = false;
        if (publishTime == null) publishTime = LocalDateTime.now();
    }
}