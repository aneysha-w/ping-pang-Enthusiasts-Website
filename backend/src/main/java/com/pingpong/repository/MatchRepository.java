package com.pingpong.repository;

import com.pingpong.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByEventIdOrderByRoundAsc(Long eventId);
    List<Match> findByEventIdAndRound(Long eventId, Integer round);
}