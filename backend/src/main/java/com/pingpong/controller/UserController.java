package com.pingpong.controller;

import com.pingpong.common.ApiResponse;
import com.pingpong.common.UserContext;
import com.pingpong.entity.User;
import com.pingpong.service.SessionService;
import com.pingpong.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final SessionService sessionService;

    public UserController(UserService userService, SessionService sessionService) {
        this.userService = userService;
        this.sessionService = sessionService;
    }

    @PostMapping("/sms-code")
    public ApiResponse<Map<String, String>> sendSmsCode(@RequestBody Map<String, String> body) {
        String code = userService.sendSmsCode(body.get("phone"));
        return ApiResponse.success(Map.of("code", code));
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, String>> register(@RequestBody Map<String, String> body) {
        String token = userService.register(body.get("phone"), body.get("password"), body.get("smsCode"));
        return ApiResponse.success(Map.of("token", token));
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String token = userService.login(body.get("phone"), body.get("password"));
        return ApiResponse.success(Map.of("token", token));
    }

    @GetMapping("/{userId}")
    public ApiResponse<User> getUser(@PathVariable Long userId) {
        return ApiResponse.success(userService.getUserById(userId));
    }

    @PutMapping("/{userId}/profile")
    public ApiResponse<User> updateProfile(@PathVariable Long userId, @RequestBody Map<String, String> body,
                                            HttpServletRequest request) {
        Long currentUserId = UserContext.getCurrentUserId(request, sessionService);
        if (!currentUserId.equals(userId)) {
            return ApiResponse.error(403, "无权操作");
        }
        User user = userService.updateProfile(userId, body.get("nickname"), body.get("realName"),
                body.get("gender"), body.get("city"), body.get("skillLevel"));
        return ApiResponse.success(user);
    }

    @PostMapping("/{userId}/avatar")
    public ApiResponse<Map<String, String>> updateAvatar(@PathVariable Long userId,
                                                          @RequestBody Map<String, String> body,
                                                          HttpServletRequest request) {
        Long currentUserId = UserContext.getCurrentUserId(request, sessionService);
        if (!currentUserId.equals(userId)) {
            return ApiResponse.error(403, "无权操作");
        }
        String url = userService.updateAvatar(userId, body.get("avatarUrl"));
        return ApiResponse.success(Map.of("avatarUrl", url));
    }

    @GetMapping("/nearby")
    public ApiResponse<List<User>> nearby(@RequestParam(required = false) String city) {
        return ApiResponse.success(userService.getNearbyUsers(city));
    }

    @GetMapping("/me")
    public ApiResponse<User> me(HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request, sessionService);
        return ApiResponse.success(userService.getUserById(userId));
    }
}