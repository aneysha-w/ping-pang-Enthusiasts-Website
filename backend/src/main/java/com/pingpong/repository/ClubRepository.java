package com.pingpong.repository;

import com.pingpong.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, Long> {
    Optional<Club> findByName(String name);
    boolean existsByName(String name);
    List<Club> findByCityAndStatus(String city, String status);
    List<Club> findByStatus(String status);
}