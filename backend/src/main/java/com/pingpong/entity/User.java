package com.pingpong.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String phone;

    private String password;
    private String nickname;
    private String realName;
    private String gender;
    private String city;
    private String skillLevel;
    private String avatarUrl;
    private Integer currentScore = 1000;
    private Integer eventScore = 0;
    private Integer friendlyScore = 0;
    private Integer matchCount = 0;
    private LocalDateTime lastMatchTime;
    private LocalDateTime registerTime;
    private LocalDateTime lastLoginTime;
    private Boolean profileCompleted = false;
    private String role = "USER";

    @PrePersist
    public void prePersist() {
        if (registerTime == null) registerTime = LocalDateTime.now();
        if (currentScore == null) currentScore = 1000;
        if (eventScore == null) eventScore = 0;
        if (friendlyScore == null) friendlyScore = 0;
        if (matchCount == null) matchCount = 0;
        if (profileCompleted == null) profileCompleted = false;
        if (role == null) role = "USER";
    }
}