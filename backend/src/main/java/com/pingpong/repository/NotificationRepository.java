package com.pingpong.repository;

import com.pingpong.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiverIdOrderByCreateTimeDesc(Long receiverId);
    List<Notification> findByReceiverIdAndIsReadFalse(Long receiverId);
}