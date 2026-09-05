package com.pingpong.controller;

import com.pingpong.common.ApiResponse;
import com.pingpong.common.BizException;
import com.pingpong.common.ErrorCode;
import com.pingpong.entity.ScoreRecord;
import com.pingpong.entity.User;
import com.pingpong.service.ClubService;
import com.pingpong.service.ScoreService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/scores")
public class ScoreController {

    private final ScoreService scoreService;
    private final ClubService clubService;

    public ScoreController(ScoreService scoreService, ClubService clubService) {
        this.scoreService = scoreService;
        this.clubService = clubService;
    }

    @GetMapping("/{userId}/history")
    public ApiResponse<List<ScoreRecord>> history(@PathVariable Long userId) {
        return ApiResponse.success(scoreService.getScoreHistory(userId));
    }

    @GetMapping("/ranking/global")
    public ApiResponse<List<User>> globalRanking() {
        return ApiResponse.success(scoreService.getGlobalRanking());
    }

    @GetMapping("/ranking/clubs/{clubId}")
    public ApiResponse<List<User>> clubRanking(@PathVariable Long clubId) {
        List<Long> memberIds = clubService.getMemberUserIds(clubId);
        return ApiResponse.success(scoreService.getClubRanking(clubId, memberIds));
    }

    @GetMapping("/ranking/by-level")
    public ApiResponse<List<User>> rankingByLevel(@RequestParam String skillLevel,
                                                    @RequestParam(defaultValue = "GLOBAL") String scope,
                                                    @RequestParam(required = false) Long clubId) {
        List<String> validLevels = List.of("BEGINNER", "INTERMEDIATE", "ADVANCED", "PROFESSIONAL");
        if (!validLevels.contains(skillLevel)) {
            throw new BizException(ErrorCode.SKILL_LEVEL_INVALID, "技术水平等级非法");
        }
        Long cId = "CLUB".equals(scope) ? clubId : null;
        List<Long> memberIds = cId != null ? clubService.getMemberUserIds(cId) : null;
        return ApiResponse.success(scoreService.getRankingByLevel(skillLevel, cId, memberIds));
    }

    @GetMapping("/ranking/grouped-by-level")
    public ApiResponse<Map<String, List<User>>> groupedByLevel() {
        return ApiResponse.success(scoreService.getRankingGroupedByLevel());
    }
}