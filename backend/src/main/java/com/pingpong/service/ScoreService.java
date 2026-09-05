package com.pingpong.service;

import com.pingpong.entity.ScoreRecord;
import com.pingpong.entity.User;
import com.pingpong.repository.ScoreRecordRepository;
import com.pingpong.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ScoreService {

    private final UserRepository userRepository;
    private final ScoreRecordRepository scoreRecordRepository;

    public ScoreService(UserRepository userRepository, ScoreRecordRepository scoreRecordRepository) {
        this.userRepository = userRepository;
        this.scoreRecordRepository = scoreRecordRepository;
    }

    public double getLevelWeight(String level) {
        return switch (level) {
            case "CHAMPIONSHIP" -> 1.5;
            case "OPEN" -> 1.2;
            case "CLUB" -> 1.0;
            case "FRIENDLY" -> 0.5;
            default -> 1.0;
        };
    }

    public int[] calculateEloChange(int scoreA, int scoreB, boolean aWins, String eventLevel) {
        double k = 32 * getLevelWeight(eventLevel);
        double expectedA = 1.0 / (1.0 + Math.pow(10, (scoreB - scoreA) / 400.0));
        double expectedB = 1.0 - expectedA;
        double actualA = aWins ? 1.0 : 0.0;
        double actualB = 1.0 - actualA;
        int changeA = (int) Math.round(k * (actualA - expectedA));
        int changeB = (int) Math.round(k * (actualB - expectedB));
        return new int[]{changeA, changeB};
    }

    public void applyScoreChange(User winner, User loser, Long eventId, Long matchId, String changeType, String eventLevel) {
        int[] changes = calculateEloChange(winner.getCurrentScore(), loser.getCurrentScore(), true, eventLevel);
        int winnerChange = changes[0];
        int loserChange = changes[1];

        int winnerBefore = winner.getCurrentScore();
        int loserBefore = loser.getCurrentScore();

        winner.setCurrentScore(winnerBefore + winnerChange);
        loser.setCurrentScore(loserBefore + loserChange);
        winner.setMatchCount(winner.getMatchCount() + 1);
        loser.setMatchCount(loser.getMatchCount() + 1);
        winner.setLastMatchTime(LocalDateTime.now());
        loser.setLastMatchTime(LocalDateTime.now());

        if ("EVENT".equals(changeType)) {
            winner.setEventScore(winner.getEventScore() + winnerChange);
            loser.setEventScore(loser.getEventScore() + loserChange);
        } else {
            winner.setFriendlyScore(winner.getFriendlyScore() + winnerChange);
            loser.setFriendlyScore(loser.getFriendlyScore() + loserChange);
        }

        userRepository.save(winner);
        userRepository.save(loser);

        saveRecord(winner.getId(), eventId, matchId, winnerChange, winnerBefore, winner.getCurrentScore(), changeType);
        saveRecord(loser.getId(), eventId, matchId, loserChange, loserBefore, loser.getCurrentScore(), changeType);
    }

    private void saveRecord(Long userId, Long eventId, Long matchId, int change, int before, int after, String type) {
        ScoreRecord record = new ScoreRecord();
        record.setUserId(userId);
        record.setEventId(eventId);
        record.setMatchId(matchId);
        record.setScoreChange(change);
        record.setScoreBefore(before);
        record.setScoreAfter(after);
        record.setChangeType(type);
        scoreRecordRepository.save(record);
    }

    public List<ScoreRecord> getScoreHistory(Long userId) {
        return scoreRecordRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    public List<User> getGlobalRanking() {
        return userRepository.findAllByOrderByCurrentScoreDescLastMatchTimeDesc();
    }

    public List<User> getClubRanking(Long clubId, List<Long> memberUserIds) {
        List<User> allUsers = userRepository.findAllByOrderByCurrentScoreDescLastMatchTimeDesc();
        return allUsers.stream()
                .filter(u -> memberUserIds.contains(u.getId()))
                .collect(Collectors.toList());
    }

    public List<User> getRankingByLevel(String skillLevel, Long clubId, List<Long> memberUserIds) {
        List<User> users;
        if (clubId != null && memberUserIds != null) {
            users = userRepository.findBySkillLevelOrderByCurrentScoreDesc(skillLevel);
            users = users.stream().filter(u -> memberUserIds.contains(u.getId())).collect(Collectors.toList());
        } else {
            users = userRepository.findBySkillLevelOrderByCurrentScoreDesc(skillLevel);
        }
        return users;
    }

    public Map<String, List<User>> getRankingGroupedByLevel() {
        List<String> levels = List.of("BEGINNER", "INTERMEDIATE", "ADVANCED", "PROFESSIONAL");
        return levels.stream().collect(Collectors.toMap(
                level -> level,
                level -> userRepository.findBySkillLevelOrderByCurrentScoreDesc(level)
        ));
    }
}