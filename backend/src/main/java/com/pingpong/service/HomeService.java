package com.pingpong.service;

import com.pingpong.entity.Club;
import com.pingpong.entity.Event;
import com.pingpong.entity.User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HomeService {

    private final UserService userService;
    private final ScoreService scoreService;
    private final EventService eventService;
    private final ClubService clubService;

    public HomeService(UserService userService, ScoreService scoreService,
                       EventService eventService, ClubService clubService) {
        this.userService = userService;
        this.scoreService = scoreService;
        this.eventService = eventService;
        this.clubService = clubService;
    }

    public Map<String, Object> getHomeData(Long userId) {
        Map<String, Object> data = new HashMap<>();

        User user = userService.getUserById(userId);
        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", user.getId());
        profile.put("nickname", user.getNickname());
        profile.put("avatarUrl", user.getAvatarUrl());
        profile.put("currentScore", user.getCurrentScore());
        profile.put("city", user.getCity());
        data.put("profile", profile);

        List<User> globalRanking = scoreService.getGlobalRanking();
        int myRank = -1;
        for (int i = 0; i < globalRanking.size(); i++) {
            if (globalRanking.get(i).getId().equals(userId)) {
                myRank = i + 1;
                break;
            }
        }
        data.put("myRank", myRank);
        data.put("rankingPreview", globalRanking.stream().limit(10).map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("userId", u.getId());
            m.put("nickname", u.getNickname());
            m.put("currentScore", u.getCurrentScore());
            return m;
        }).toList());

        List<Event> events = eventService.listEvents(null, null);
        data.put("recentEvents", events.stream().limit(5).map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("eventId", e.getId());
            m.put("name", e.getName());
            m.put("level", e.getLevel());
            m.put("status", e.getStatus());
            m.put("startTime", e.getStartTime());
            return m;
        }).toList());

        List<Club> clubs = clubService.getNearbyClubs(user.getCity());
        data.put("nearbyClubs", clubs.stream().limit(5).map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("clubId", c.getId());
            m.put("name", c.getName());
            m.put("city", c.getCity());
            m.put("memberCount", c.getMemberCount());
            return m;
        }).toList());

        return data;
    }
}