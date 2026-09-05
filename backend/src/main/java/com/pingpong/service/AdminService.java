package com.pingpong.service;

import com.pingpong.common.BizException;
import com.pingpong.common.ErrorCode;
import com.pingpong.entity.*;
import com.pingpong.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminService {

    private final SystemAdminAccountRepository adminAccountRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final ClubRepository clubRepository;
    private final SessionService sessionService;
    private final NotifyService notifyService;
    private final UserService userService;

    @Value("${app.admin.account:admin}")
    private String adminAccount;
    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    public AdminService(SystemAdminAccountRepository adminAccountRepository,
                        JoinRequestRepository joinRequestRepository,
                        ClubMemberRepository clubMemberRepository,
                        ClubRepository clubRepository,
                        SessionService sessionService,
                        NotifyService notifyService,
                        UserService userService) {
        this.adminAccountRepository = adminAccountRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.clubMemberRepository = clubMemberRepository;
        this.clubRepository = clubRepository;
        this.sessionService = sessionService;
        this.notifyService = notifyService;
        this.userService = userService;
    }

    public void initAdminAccount() {
        if (!adminAccountRepository.existsByAccount(adminAccount)) {
            SystemAdminAccount account = new SystemAdminAccount();
            account.setAccount(adminAccount);
            account.setPasswordHash(userService.hashPassword(adminPassword));
            adminAccountRepository.save(account);
        }
    }

    public String login(String account, String password) {
        SystemAdminAccount admin = adminAccountRepository.findByAccount(account)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_SYSTEM_ADMIN, "非系统管理员账号"));
        if (!userService.hashPassword(password).equals(admin.getPasswordHash())) {
            throw new BizException(ErrorCode.ADMIN_PASSWORD_ERROR, "账号或密码错误");
        }
        return sessionService.createAdminToken(admin.getId());
    }

    public List<JoinRequest> getJoinRequests(String status) {
        if (status == null || status.isEmpty()) {
            return joinRequestRepository.findAllByOrderByApplyTimeDesc();
        }
        return joinRequestRepository.findByStatusOrderByApplyTimeDesc(status);
    }

    @Transactional
    public void approveJoinRequest(Long requestId, Long approverId, boolean approved, String rejectReason) {
        JoinRequest req = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new BizException(ErrorCode.REQUEST_NOT_FOUND, "申请不存在"));
        if (!"PENDING".equals(req.getStatus())) {
            throw new BizException(ErrorCode.REQUEST_ALREADY_PROCESSED, "该申请已处理");
        }

        req.setApproverId(approverId);
        req.setApproveTime(LocalDateTime.now());

        if (approved) {
            req.setStatus("APPROVED");
            joinRequestRepository.save(req);

            ClubMember member = new ClubMember();
            member.setClubId(req.getClubId());
            member.setUserId(req.getApplicantId());
            member.setRole("MEMBER");
            clubMemberRepository.save(member);

            Club club = clubRepository.findById(req.getClubId()).orElseThrow();
            club.setMemberCount(club.getMemberCount() + 1);
            clubRepository.save(club);

            notifyService.sendNotification(req.getApplicantId(), "CLUB_JOIN_AUDIT",
                    "加入申请已通过", "您加入俱乐部" + club.getName() + "的申请已通过", req.getClubId());
        } else {
            req.setStatus("REJECTED");
            req.setRejectReason(rejectReason);
            joinRequestRepository.save(req);

            Club club = clubRepository.findById(req.getClubId()).orElseThrow();
            notifyService.sendNotification(req.getApplicantId(), "CLUB_JOIN_AUDIT",
                    "加入申请已拒绝", "您加入俱乐部" + club.getName() + "的申请已被拒绝" +
                            (rejectReason != null ? "，原因: " + rejectReason : ""), req.getClubId());
        }
    }
}