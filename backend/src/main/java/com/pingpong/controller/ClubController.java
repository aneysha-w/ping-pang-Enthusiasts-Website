package com.pingpong.controller;

import com.pingpong.common.ApiResponse;
import com.pingpong.common.UserContext;
import com.pingpong.entity.*;
import com.pingpong.service.ClubService;
import com.pingpong.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/clubs")
public class ClubController {

    private final ClubService clubService;
    private final SessionService sessionService;

    public ClubController(ClubService clubService, SessionService sessionService) {
        this.clubService = clubService;
        this.sessionService = sessionService;
    }

    @PostMapping
    public ApiResponse<Club> create(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request, sessionService);
        Club club = clubService.createClub(
                (String) body.get("name"),
                (String) body.get("city"),
                (String) body.get("description"),
                (String) body.get("logoUrl"),
                userId
        );
        return ApiResponse.success(club);
    }

    @GetMapping
    public ApiResponse<List<Club>> list(@RequestParam(required = false) String city) {
        return ApiResponse.success(clubService.getNearbyClubs(city));
    }

    @GetMapping("/nearby")
    public ApiResponse<List<Club>> nearby(@RequestParam(required = false) String city) {
        return ApiResponse.success(clubService.getNearbyClubs(city));
    }

    @GetMapping("/{clubId}")
    public ApiResponse<Club> get(@PathVariable Long clubId) {
        return ApiResponse.success(clubService.getClub(clubId));
    }

    @GetMapping("/{clubId}/members")
    public ApiResponse<List<ClubMember>> members(@PathVariable Long clubId) {
        return ApiResponse.success(clubService.getMembers(clubId));
    }

    @PostMapping("/{clubId}/join-requests")
    public ApiResponse<JoinRequest> joinRequest(@PathVariable Long clubId, HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request, sessionService);
        return ApiResponse.success(clubService.requestJoin(clubId, userId));
    }

    @DeleteMapping("/{clubId}/members/{memberId}")
    public ApiResponse<Void> removeMember(@PathVariable Long clubId, @PathVariable Long memberId,
                                           HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request, sessionService);
        clubService.removeMember(clubId, memberId, userId);
        return ApiResponse.success();
    }

    @PutMapping("/{clubId}/admin")
    public ApiResponse<Void> transferAdmin(@PathVariable Long clubId, @RequestBody Map<String, Object> body,
                                            HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request, sessionService);
        Long newAdminId = Long.valueOf(body.get("newAdminUserId").toString());
        clubService.transferAdmin(clubId, newAdminId, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{clubId}/posts")
    public ApiResponse<Post> createPost(@PathVariable Long clubId, @RequestBody Map<String, String> body,
                                        HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request, sessionService);
        return ApiResponse.success(clubService.createPost(clubId, userId, body.get("title"), body.get("content")));
    }

    @PutMapping("/{clubId}/posts/{postId}")
    public ApiResponse<Post> editPost(@PathVariable Long clubId, @PathVariable Long postId,
                                      @RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request, sessionService);
        return ApiResponse.success(clubService.editPost(clubId, postId, userId, body.get("title"), body.get("content")));
    }

    @DeleteMapping("/{clubId}/posts/{postId}")
    public ApiResponse<Void> deletePost(@PathVariable Long clubId, @PathVariable Long postId,
                                        HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request, sessionService);
        clubService.deletePost(clubId, postId, userId);
        return ApiResponse.success();
    }

    @GetMapping("/{clubId}/posts")
    public ApiResponse<List<Post>> posts(@PathVariable Long clubId) {
        return ApiResponse.success(clubService.getPosts(clubId));
    }

    @PostMapping("/{clubId}/posts/{postId}/comments")
    public ApiResponse<Comment> addComment(@PathVariable Long clubId, @PathVariable Long postId,
                                           @RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request, sessionService);
        return ApiResponse.success(clubService.addComment(clubId, postId, userId, body.get("content")));
    }

    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<List<Comment>> comments(@PathVariable Long postId) {
        return ApiResponse.success(clubService.getComments(postId));
    }

    @DeleteMapping("/{clubId}")
    public ApiResponse<Void> dissolve(@PathVariable Long clubId, HttpServletRequest request) {
        Long userId = UserContext.getCurrentUserId(request, sessionService);
        clubService.dissolveClub(clubId, userId);
        return ApiResponse.success();
    }
}