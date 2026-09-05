package com.pingpong.repository;

import com.pingpong.entity.JoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {
    Optional<JoinRequest> findByClubIdAndApplicantIdAndStatus(Long clubId, Long applicantId, String status);
    List<JoinRequest> findByStatusOrderByApplyTimeDesc(String status);
    List<JoinRequest> findByClubIdAndStatus(Long clubId, String status);
    List<JoinRequest> findAllByOrderByApplyTimeDesc();
}