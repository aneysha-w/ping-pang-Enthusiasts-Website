package com.pingpong.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "system_admin_account")
public class SystemAdminAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String account;
    private String passwordHash;
    private String status = "ACTIVE";
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (status == null) status = "ACTIVE";
        if (createTime == null) createTime = LocalDateTime.now();
    }
}