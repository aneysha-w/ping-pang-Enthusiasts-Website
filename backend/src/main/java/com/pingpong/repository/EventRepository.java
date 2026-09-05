package com.pingpong.repository;

import com.pingpong.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatusOrderByStartTimeDesc(String status);
    List<Event> findByClubIdAndStatus(Long clubId, String status);
    List<Event> findAllByOrderByStartTimeDesc();
}