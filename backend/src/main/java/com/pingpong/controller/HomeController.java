package com.pingpong.controller;

import com.pingpong.common.ApiResponse;
import com.pingpong.common.UserContext;
import com.pingpong.entity.Notification;
import com.pingpong.repository.NotificationRepository;
import com.pingpong.service.HomeService;
import com.pingpong.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class HomeController {

    private final HomeService homeService;
    private final SessionService sessionService;
    private final NotificationRepository notificationRepository;

    public HomeController(HomeService homeService, SessionService sessionService,
                          NotificationRepository notificationRepository) {
        this.homeService = homeService;
        this.sessionService = sessionService;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/home")
    public ApiResponse<Map<String, Object>> home(HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request, sessionService);
        return ApiResponse.success(homeService.getHomeData(userId));
    }

    @GetMapping("/notifications")
    public ApiResponse<List<Notification>> notifications(HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request, sessionService);
        return ApiResponse.success(notificationRepository.findByReceiverIdOrderByCreateTimeDesc(userId));
    }
}