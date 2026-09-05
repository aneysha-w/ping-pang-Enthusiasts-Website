package com.pingpong.service;

import com.pingpong.common.BizException;
import com.pingpong.common.ErrorCode;
import com.pingpong.entity.*;
import com.pingpong.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final ScoreService scoreService;
    private final NotifyService notifyService;
    private final ClubMemberRepository clubMemberRepository;

    public EventService(EventRepository eventRepository, EnrollmentRepository enrollmentRepository,
                        MatchRepository matchRepository, UserRepository userRepository,
                        ScoreService scoreService, NotifyService notifyService,
                        ClubMemberRepository clubMemberRepository) {
        this.eventRepository = eventRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.scoreService = scoreService;
        this.notifyService = notifyService;
        this.clubMemberRepository = clubMemberRepository;
    }

    public Event createEvent(String name, String level, String format, Integer maxPlayers,
                             LocalDateTime startTime, LocalDateTime enrollDeadline,
                             String location, Long organizerId, Long clubId) {
        if (name == null || name.isBlank() || level == null || format == null
                || maxPlayers == null || startTime == null || enrollDeadline == null || location == null) {
            throw new BizException(ErrorCode.EVENT_FIELD_INVALID, "赛事字段不完整");
        }
        if (maxPlayers < 4 || maxPlayers > 256) {
            throw new BizException(ErrorCode.EVENT_FIELD_INVALID, "赛事人数需在4-256之间");
        }
        if ("KNOCKOUT".equals(format) && !isPowerOfTwo(maxPlayers)) {
            throw new BizException(ErrorCode.EVENT_PLAYERS_NOT_POWER_OF_2, "淘汰赛人数需为2的幂次方");
        }
        if (!enrollDeadline.isBefore(startTime) || java.time.Duration.between(enrollDeadline, startTime).toHours() < 1) {
            throw new BizException(ErrorCode.EVENT_DEADLINE_INVALID, "报名截止需早于开始时间至少1小时");
        }

        Event event = new Event();
        event.setName(name);
        event.setLevel(level);
        event.setFormat(format);
        event.setMaxPlayers(maxPlayers);
        event.setStartTime(startTime);
        event.setEnrollDeadline(enrollDeadline);
        event.setLocation(location);
        event.setOrganizerId(organizerId);
        event.setClubId(clubId);
        return eventRepository.save(event);
    }

    public Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new BizException(ErrorCode.EVENT_FIELD_INVALID, "赛事不存在"));
    }

    public List<Event> listEvents(String status, String level) {
        List<Event> all = eventRepository.findAllByOrderByStartTimeDesc();
        return all.stream()
                .filter(e -> status == null || status.equals(e.getStatus()))
                .filter(e -> level == null || level.equals(e.getLevel()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void enroll(Long eventId, Long userId) {
        Event event = getEvent(eventId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.USER_NOT_FOUND, "用户不存在"));

        if (user.getProfileCompleted() == null || !user.getProfileCompleted()) {
            throw new BizException(ErrorCode.PROFILE_NOT_COMPLETE, "请先完善个人资料");
        }
        if (event.getClubId() != null) {
            if (!clubMemberRepository.existsByClubIdAndUserIdAndStatus(event.getClubId(), userId, "ACTIVE")) {
                throw new BizException(ErrorCode.CLUB_MEMBER_ONLY, "仅限本俱乐部成员报名");
            }
        }
        if (LocalDateTime.now().isAfter(event.getEnrollDeadline())) {
            throw new BizException(ErrorCode.ENROLL_CLOSED, "报名已截止");
        }
        if (event.getEnrolledCount() >= event.getMaxPlayers()) {
            throw new BizException(ErrorCode.EVENT_FULL, "名额已满");
        }
        if (enrollmentRepository.findByEventIdAndUserId(eventId, userId).isPresent()) {
            throw new BizException(ErrorCode.EVENT_FULL, "已报名该赛事");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setEventId(eventId);
        enrollment.setUserId(userId);
        enrollmentRepository.save(enrollment);

        event.setEnrolledCount(event.getEnrolledCount() + 1);
        eventRepository.save(event);

        notifyService.sendNotification(userId, "EVENT_ENROLL", "报名成功",
                "您已成功报名赛事: " + event.getName(), eventId);
    }

    @Transactional
    public void cancelEnroll(Long eventId, Long userId) {
        Event event = getEvent(eventId);
        if (LocalDateTime.now().isAfter(event.getEnrollDeadline())) {
            throw new BizException(ErrorCode.ENROLL_CLOSED, "报名已截止，无法取消");
        }
        Enrollment enrollment = enrollmentRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_ENROLLED, "未报名该赛事"));
        enrollmentRepository.delete(enrollment);
        event.setEnrolledCount(Math.max(0, event.getEnrolledCount() - 1));
        eventRepository.save(event);
    }

    public List<Enrollment> getEnrollments(Long eventId) {
        return enrollmentRepository.findByEventId(eventId);
    }

    @Transactional
    public List<Match> generateSchedule(Long eventId, int seedCount) {
        Event event = getEvent(eventId);
        if (!"PENDING".equals(event.getStatus())) {
            throw new BizException(ErrorCode.EVENT_STATUS_NOT_ALLOW_SCHEDULE, "赛事状态不允许生成赛程");
        }
        List<Enrollment> enrollments = enrollmentRepository.findByEventId(eventId);
        if (enrollments.size() < 4) {
            throw new BizException(ErrorCode.EVENT_PLAYERS_NOT_ENOUGH, "报名人数不足");
        }

        List<Long> playerIds = enrollments.stream()
                .map(Enrollment::getUserId)
                .collect(Collectors.toList());

        List<User> players = userRepository.findAllById(playerIds);
        players.sort(Comparator.comparing(User::getCurrentScore).reversed());

        List<Long> sortedIds = players.stream().map(User::getId).collect(Collectors.toList());

        List<Match> matches;
        if ("KNOCKOUT".equals(event.getFormat())) {
            matches = generateKnockout(eventId, sortedIds, seedCount);
        } else {
            matches = generateRoundRobin(eventId, sortedIds);
        }

        event.setStatus("IN_PROGRESS");
        eventRepository.save(event);
        return matches;
    }

    private List<Match> generateKnockout(Long eventId, List<Long> playerIds, int seedCount) {
        int n = playerIds.size();
        int nextPow2 = 1;
        while (nextPow2 < n) nextPow2 *= 2;

        List<Long> bracket = new ArrayList<>(Collections.nCopies(nextPow2, null));
        for (int i = 0; i < n; i++) bracket.set(i, playerIds.get(i));

        List<Match> matches = new ArrayList<>();
        for (int i = 0; i < nextPow2; i += 2) {
            Match m = new Match(eventId, 1, bracket.get(i), bracket.get(i + 1));
            if (m.getPlayerB() == null) {
                m.setIsBye(true);
                m.setStatus("FINISHED");
                m.setWinnerId(m.getPlayerA());
            }
            matches.add(matchRepository.save(m));
        }
        return matches;
    }

    private List<Match> generateRoundRobin(Long eventId, List<Long> playerIds) {
        int n = playerIds.size();
        List<Long> ids = new ArrayList<>(playerIds);
        if (n % 2 != 0) {
            ids.add(null);
            n++;
        }

        List<Match> matches = new ArrayList<>();
        int rounds = n - 1;
        for (int r = 0; r < rounds; r++) {
            for (int i = 0; i < n / 2; i++) {
                int a = (r + i) % (n - 1);
                int b = (n - 1 - i + r) % (n - 1);
                Long playerA = ids.get(i == 0 ? n - 1 : a);
                Long playerB = ids.get(i == 0 ? a : b);
                if (playerA != null && playerB != null) {
                    matches.add(matchRepository.save(new Match(eventId, r + 1, playerA, playerB)));
                }
            }
        }
        return matches;
    }

    public List<Match> getMatches(Long eventId) {
        return matchRepository.findByEventIdOrderByRoundAsc(eventId);
    }

    @Transactional
    public void recordResult(Long eventId, Long matchId, Long winnerId, Long operatorId) {
        Event event = getEvent(eventId);
        if (!"IN_PROGRESS".equals(event.getStatus())) {
            throw new BizException(ErrorCode.EVENT_STATUS_INVALID, "赛事状态非法");
        }
        if (!event.getOrganizerId().equals(operatorId)) {
            throw new BizException(ErrorCode.NO_PERMISSION_RESULT, "无权录入比赛结果");
        }

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new BizException(ErrorCode.EVENT_FIELD_INVALID, "对阵不存在"));
        if ("FINISHED".equals(match.getStatus())) {
            return;
        }

        Long loserId = match.getPlayerA().equals(winnerId) ? match.getPlayerB() : match.getPlayerA();
        User winner = userRepository.findById(winnerId).orElseThrow();
        User loser = userRepository.findById(loserId).orElseThrow();

        String changeType = "EVENT";
        scoreService.applyScoreChange(winner, loser, eventId, matchId, changeType, event.getLevel());

        match.setWinnerId(winnerId);
        match.setStatus("FINISHED");
        matchRepository.save(match);

        notifyService.sendNotification(winnerId, "SCORE_CHANGE", "积分变动",
                "您在赛事" + event.getName() + "中获胜，积分已更新", eventId);
        notifyService.sendNotification(loserId, "SCORE_CHANGE", "积分变动",
                "您在赛事" + event.getName() + "中失利，积分已更新", eventId);
    }

    @Transactional
    public void updateStatus(Long eventId, String newStatus, Long operatorId) {
        Event event = getEvent(eventId);
        String current = event.getStatus();
        boolean valid = ("PENDING".equals(current) && "IN_PROGRESS".equals(newStatus))
                || ("IN_PROGRESS".equals(current) && "FINISHED".equals(newStatus))
                || ("PENDING".equals(current) && "CANCELLED".equals(newStatus));
        if (!valid) {
            throw new BizException(ErrorCode.EVENT_STATUS_TRANSITION_INVALID, "非法状态流转");
        }
        event.setStatus(newStatus);
        eventRepository.save(event);

        if ("CANCELLED".equals(newStatus)) {
            List<Enrollment> enrollments = enrollmentRepository.findByEventId(eventId);
            enrollments.forEach(e -> notifyService.sendNotification(e.getUserId(), "EVENT_CANCEL",
                    "赛事取消", "赛事" + event.getName() + "已取消", eventId));
        }
    }

    @Transactional
    public void cancelEvent(Long eventId, Long operatorId) {
        Event event = getEvent(eventId);
        if (!"PENDING".equals(event.getStatus())) {
            throw new BizException(ErrorCode.EVENT_STARTED_CANNOT_CANCEL, "赛事已开始，不可取消");
        }
        event.setStatus("CANCELLED");
        eventRepository.save(event);
        List<Enrollment> enrollments = enrollmentRepository.findByEventId(eventId);
        enrollments.forEach(e -> notifyService.sendNotification(e.getUserId(), "EVENT_CANCEL",
                "赛事取消", "赛事" + event.getName() + "已取消", eventId));
    }

    private boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }
}