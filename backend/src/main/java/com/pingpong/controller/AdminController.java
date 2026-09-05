package com.pingpong.controller;

import com.pingpong.common.ApiResponse;
import com.pingpong.common.UserContext;
import com.pingpong.entity.JoinRequest;
import com.pingpong.service.AdminService;
import com.pingpong.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final SessionService sessionService;

    public AdminController(AdminService adminService, SessionService sessionService) {
        this.adminService = adminService;
        this.sessionService = sessionService;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String token = adminService.login(body.get("account"), body.get("password"));
        return ApiResponse.success(Map.of("token", token));
    }

    @GetMapping("/join-requests")
    public ApiResponse<List<JoinRequest>> joinRequests(@RequestParam(required = false) String status,
                                                        HttpServletRequest request) {
        UserContext.getCurrentAdminId(request, sessionService);
        return ApiResponse.success(adminService.getJoinRequests(status));
    }

    @PutMapping("/join-requests/{requestId}")
    public ApiResponse<Void> approve(@PathVariable Long requestId, @RequestBody Map<String, Object> body,
                                     HttpServletRequest request) {
        Long adminId = UserContext.getCurrentAdminId(request, sessionService);
        boolean approved = Boolean.TRUE.equals(body.get("approved"));
        String rejectReason = body.get("rejectReason") != null ? body.get("rejectReason").toString() : null;
        adminService.approveJoinRequest(requestId, adminId, approved, rejectReason);
        return ApiResponse.success();
    }
}