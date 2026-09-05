package com.pingpong.controller;

import com.pingpong.common.ApiResponse;
import com.pingpong.common.UserContext;
import com.pingpong.entity.Enrollment;
import com.pingpong.entity.Event;
import com.pingpong.entity.Match;
import com.pingpong.service.EventService;
import com.pingpong.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final SessionService sessionService;

    public EventController(EventService eventService, SessionService sessionService) {
        this.eventService = eventService;
        this.sessionService = sessionService;
    }

    @PostMapping
    public ApiResponse<Event> create(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request, sessionService);
        Long clubId = body.get("clubId") != null ? Long.valueOf(body.get("clubId").toString()) : null;
        Event event = eventService.createEvent(
                (String) body.get("name"),
                (String) body.get("level"),
                (String) body.get("format"),
                body.get("maxPlayers") != null ? Integer.valueOf(body.get("maxPlayers").toString()) : null,
                body.get("startTime") != null ? LocalDateTime.parse(body.get("startTime").toString()) : null,
                body.get("enrollDeadline") != null ? LocalDateTime.parse(body.get("enrollDeadline").toString()) : null,
                (String) body.get("location"),
                userId,
                clubId
        );
        return ApiResponse.success(event);
    }

    @GetMapping
    public ApiResponse<List<Event>> list(@RequestParam(required = false) String status,
                                          @RequestParam(required = false) String level) {
        return ApiResponse.success(eventService.listEvents(status, level));
    }

    @GetMapping("/{eventId}")
    public ApiResponse<Event> get(@PathVariable Long eventId) {
        return ApiResponse.success(eventService.getEvent(eventId));
    }

    @PostMapping("/{eventId}/enroll")
    public ApiResponse<Void> enroll(@PathVariable Long eventId, HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request, sessionService);
        eventService.enroll(eventId, userId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{eventId}/enroll")
    public ApiResponse<Void> cancelEnroll(@PathVariable Long eventId, HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request, sessionService);
        eventService.cancelEnroll(eventId, userId);
        return ApiResponse.success();
    }

    @GetMapping("/{eventId}/enrollments")
    public ApiResponse<List<Enrollment>> enrollments(@PathVariable Long eventId) {
        return ApiResponse.success(eventService.getEnrollments(eventId));
    }

    @PostMapping("/{eventId}/schedule")
    public ApiResponse<List<Match>> schedule(@PathVariable Long eventId,
                                              @RequestParam(defaultValue = "0") int seedCount,
                                              HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request, sessionService);
        return ApiResponse.success(eventService.generateSchedule(eventId, seedCount));
    }

    @GetMapping("/{eventId}/matches")
    public ApiResponse<List<Match>> matches(@PathVariable Long eventId) {
        return ApiResponse.success(eventService.getMatches(eventId));
    }

    @PostMapping("/{eventId}/matches/{matchId}/result")
    public ApiResponse<Void> recordResult(@PathVariable Long eventId, @PathVariable Long matchId,
                                           @RequestBody Map<String, Object> body,
                                           HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request, sessionService);
        Long winnerId = Long.valueOf(body.get("winnerId").toString());
        eventService.recordResult(eventId, matchId, winnerId, userId);
        return ApiResponse.success();
    }

    @PutMapping("/{eventId}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long eventId,
                                           @RequestBody Map<String, String> body,
                                           HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request, sessionService);
        eventService.updateStatus(eventId, body.get("status"), userId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{eventId}")
    public ApiResponse<Void> cancel(@PathVariable Long eventId, HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request, sessionService);
        eventService.cancelEvent(eventId, userId);
        return ApiResponse.success();
    }
}