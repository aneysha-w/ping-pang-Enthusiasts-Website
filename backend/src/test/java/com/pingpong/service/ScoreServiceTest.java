package com.pingpong.service;

import com.pingpong.entity.User;
import com.pingpong.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ScoreServiceTest {

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testEloCalculation() {
        int[] changes = scoreService.calculateEloChange(1000, 1000, true, "CHAMPIONSHIP");
        assertTrue(changes[0] > 0, "胜者积分应增加");
        assertTrue(changes[1] < 0, "败者积分应减少");
        assertEquals(-changes[0], -changes[1], "同积分选手变动幅度应相同");
    }

    @Test
    void testEloLowBeatsHigh() {
        int[] changesLowWins = scoreService.calculateEloChange(800, 1200, true, "CHAMPIONSHIP");
        int[] changesHighWins = scoreService.calculateEloChange(1200, 800, true, "CHAMPIONSHIP");
        assertTrue(changesLowWins[0] > changesHighWins[0],
                "低分胜高分应获得更多积分");
    }

    @Test
    void testLevelWeight() {
        assertEquals(1.5, scoreService.getLevelWeight("CHAMPIONSHIP"));
        assertEquals(1.2, scoreService.getLevelWeight("OPEN"));
        assertEquals(1.0, scoreService.getLevelWeight("CLUB"));
        assertEquals(0.5, scoreService.getLevelWeight("FRIENDLY"));
    }

    @Test
    void testGlobalRanking() {
        List<User> ranking = scoreService.getGlobalRanking();
        assertNotNull(ranking);
        for (int i = 1; i < ranking.size(); i++) {
            assertTrue(ranking.get(i - 1).getCurrentScore() >= ranking.get(i).getCurrentScore(),
                    "排行榜应按积分降序排列");
        }
    }
}