package com.pingpong.common;

import com.pingpong.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;

public class UserContext {
    public static Long getCurrentUserId(HttpServletRequest request, SessionService sessionService) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = sessionService.getUserId(token);
        if (userId == null) {
            throw new BizException(90002, "未登录或登录已过期");
        }
        return userId;
    }

    public static Long getCurrentUserIdOptional(HttpServletRequest request, SessionService sessionService) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return sessionService.getUserId(token);
    }

    public static Long getCurrentAdminId(HttpServletRequest request, SessionService sessionService) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long adminId = sessionService.getAdminId(token);
        if (adminId == null) {
            throw new BizException(403, "无管理后台权限");
        }
        return adminId;
    }
}