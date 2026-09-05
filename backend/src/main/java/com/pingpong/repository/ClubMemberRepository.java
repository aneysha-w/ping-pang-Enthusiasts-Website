package com.pingpong.repository;

import com.pingpong.entity.ClubMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
    Optional<ClubMember> findByClubIdAndUserId(Long clubId, Long userId);
    List<ClubMember> findByClubIdAndStatus(Long clubId, String status);
    boolean existsByClubIdAndUserIdAndStatus(Long clubId, Long userId, String status);
    long countByClubIdAndStatus(Long clubId, String status);
}