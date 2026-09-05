package com.pingpong.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "club")
public class Club {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;
    private Long creatorId;
    private String city;
    private String description;
    private String logoUrl;
    private Integer memberCount = 1;
    private String status = "ACTIVE";
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (memberCount == null) memberCount = 1;
        if (status == null) status = "ACTIVE";
        if (createTime == null) createTime = LocalDateTime.now();
    }
}