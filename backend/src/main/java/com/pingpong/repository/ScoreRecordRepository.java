package com.pingpong.repository;

import com.pingpong.entity.ScoreRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScoreRecordRepository extends JpaRepository<ScoreRecord, Long> {
    List<ScoreRecord> findByUserIdOrderByCreateTimeDesc(Long userId);
}