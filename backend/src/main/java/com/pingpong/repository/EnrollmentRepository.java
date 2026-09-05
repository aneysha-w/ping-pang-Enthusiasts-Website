package com.pingpong.repository;

import com.pingpong.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByEventIdAndUserId(Long eventId, Long userId);
    List<Enrollment> findByEventId(Long eventId);
    List<Enrollment> findByUserId(Long userId);
    long countByEventId(Long eventId);
}