package com.pingpong.service;

import com.pingpong.entity.Notification;
import com.pingpong.repository.NotificationRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotifyService {

    private final NotificationRepository notificationRepository;

    public NotifyService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Async
    public void sendNotification(Long receiverId, String type, String title, String content, Long relatedBusinessId) {
        Notification n = new Notification();
        n.setReceiverId(receiverId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setRelatedBusinessId(relatedBusinessId);
        notificationRepository.save(n);
    }
}