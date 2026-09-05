package com.pingpong.service;

import com.pingpong.common.BizException;
import com.pingpong.common.ErrorCode;
import com.pingpong.entity.*;
import com.pingpong.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClubService {

    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final SensitiveService sensitiveService;
    private final NotifyService notifyService;

    public ClubService(ClubRepository clubRepository, ClubMemberRepository clubMemberRepository,
                       JoinRequestRepository joinRequestRepository, PostRepository postRepository,
                       CommentRepository commentRepository, UserRepository userRepository,
                       EventRepository eventRepository, SensitiveService sensitiveService,
                       NotifyService notifyService) {
        this.clubRepository = clubRepository;
        this.clubMemberRepository = clubMemberRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.sensitiveService = sensitiveService;
        this.notifyService = notifyService;
    }

    @Transactional
    public Club createClub(String name, String city, String description, String logoUrl, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new BizException(ErrorCode.USER_NOT_FOUND, "用户不存在"));
        if (creator.getProfileCompleted() == null || !creator.getProfileCompleted()) {
            throw new BizException(ErrorCode.PROFILE_INVALID, "请先完善个人资料");
        }
        if (clubRepository.existsByName(name)) {
            throw new BizException(ErrorCode.CLUB_NAME_EXISTS, "俱乐部名称已存在");
        }

        Club club = new Club();
        club.setName(name);
        club.setCity(city);
        club.setDescription(description);
        club.setLogoUrl(logoUrl);
        club.setCreatorId(creatorId);
        club = clubRepository.save(club);

        ClubMember member = new ClubMember();
        member.setClubId(club.getId());
        member.setUserId(creatorId);
        member.setRole("ADMIN");
        clubMemberRepository.save(member);

        return club;
    }

    public Club getClub(Long clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new BizException(ErrorCode.USER_NOT_FOUND, "俱乐部不存在"));
    }

    public List<Club> getNearbyClubs(String city) {
        if (city == null || city.isEmpty()) {
            return clubRepository.findByStatus("ACTIVE");
        }
        return clubRepository.findByCityAndStatus(city, "ACTIVE");
    }

    public List<Club> getAllClubs() {
        return clubRepository.findByStatus("ACTIVE");
    }

    @Transactional
    public JoinRequest requestJoin(Long clubId, Long userId) {
        if (clubMemberRepository.existsByClubIdAndUserIdAndStatus(clubId, userId, "ACTIVE")) {
            throw new BizException(ErrorCode.NOT_CLUB_MEMBER, "已是俱乐部成员");
        }
        if (joinRequestRepository.findByClubIdAndApplicantIdAndStatus(clubId, userId, "PENDING").isPresent()) {
            throw new BizException(ErrorCode.NOT_CLUB_MEMBER, "已有待审核申请");
        }

        JoinRequest req = new JoinRequest();
        req.setClubId(clubId);
        req.setApplicantId(userId);
        req = joinRequestRepository.save(req);

        List<ClubMember> admins = clubMemberRepository.findByClubIdAndStatus(clubId, "ACTIVE").stream()
                .filter(m -> "ADMIN".equals(m.getRole()))
                .collect(Collectors.toList());
        Club club = getClub(clubId);
        admins.forEach(a -> notifyService.sendNotification(a.getUserId(), "CLUB_JOIN_REQUEST",
                "新的加入申请", "有新用户申请加入俱乐部: " + club.getName(), req.getId()));

        return req;
    }

    public List<ClubMember> getMembers(Long clubId) {
        return clubMemberRepository.findByClubIdAndStatus(clubId, "ACTIVE");
    }

    public boolean isMember(Long clubId, Long userId) {
        return clubMemberRepository.existsByClubIdAndUserIdAndStatus(clubId, userId, "ACTIVE");
    }

    public boolean isAdmin(Long clubId, Long userId) {
        return clubMemberRepository.findByClubIdAndUserId(clubId, userId)
                .filter(m -> "ADMIN".equals(m.getRole()) && "ACTIVE".equals(m.getStatus()))
                .isPresent();
    }

    @Transactional
    public void removeMember(Long clubId, Long memberId, Long operatorId) {
        if (!isAdmin(clubId, operatorId)) {
            throw new BizException(ErrorCode.NOT_CLUB_MEMBER, "无权操作");
        }
        ClubMember member = clubMemberRepository.findById(memberId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_CLUB_MEMBER, "成员不存在"));
        member.setStatus("REMOVED");
        clubMemberRepository.save(member);

        Club club = getClub(clubId);
        club.setMemberCount(Math.max(0, club.getMemberCount() - 1));
        clubRepository.save(club);

        List<Post> posts = postRepository.findByClubIdAndStatusOrderByPublishTimeDesc(clubId, "ACTIVE");
        posts.stream().filter(p -> p.getAuthorId().equals(member.getUserId())).forEach(p -> {
            p.setFormerMemberPost(true);
            postRepository.save(p);
        });

        notifyService.sendNotification(member.getUserId(), "CLUB_REMOVED",
                "被移出俱乐部", "您已被移出俱乐部: " + club.getName(), clubId);
    }

    @Transactional
    public void transferAdmin(Long clubId, Long newAdminUserId, Long operatorId) {
        if (!isAdmin(clubId, operatorId)) {
            throw new BizException(ErrorCode.NOT_CLUB_MEMBER, "无权操作");
        }
        ClubMember oldAdmin = clubMemberRepository.findByClubIdAndUserId(clubId, operatorId).orElseThrow();
        ClubMember newAdmin = clubMemberRepository.findByClubIdAndUserId(clubId, newAdminUserId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_CLUB_MEMBER, "目标成员不存在"));
        oldAdmin.setRole("MEMBER");
        newAdmin.setRole("ADMIN");
        clubMemberRepository.save(oldAdmin);
        clubMemberRepository.save(newAdmin);
    }

    @Transactional
    public Post createPost(Long clubId, Long authorId, String title, String content) {
        if (!isMember(clubId, authorId)) {
            throw new BizException(ErrorCode.NOT_CLUB_MEMBER, "请先加入俱乐部");
        }
        if (sensitiveService.containsSensitive(title) || sensitiveService.containsSensitive(content)) {
            throw new BizException(ErrorCode.CONTENT_SENSITIVE, "内容包含违规信息");
        }
        Post post = new Post();
        post.setClubId(clubId);
        post.setAuthorId(authorId);
        post.setTitle(title);
        post.setContent(content);
        return postRepository.save(post);
    }

    @Transactional
    public Post editPost(Long clubId, Long postId, Long userId, String title, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BizException(ErrorCode.USER_NOT_FOUND, "帖子不存在"));
        if (!post.getAuthorId().equals(userId)) {
            throw new BizException(ErrorCode.NO_PERMISSION_EDIT_POST, "无权编辑他人帖子");
        }
        if (sensitiveService.containsSensitive(title) || sensitiveService.containsSensitive(content)) {
            throw new BizException(ErrorCode.CONTENT_SENSITIVE, "内容包含违规信息");
        }
        post.setTitle(title);
        post.setContent(content);
        post.setEditTime(LocalDateTime.now());
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long clubId, Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BizException(ErrorCode.USER_NOT_FOUND, "帖子不存在"));
        if (!post.getAuthorId().equals(userId) && !isAdmin(clubId, userId)) {
            throw new BizException(ErrorCode.NO_PERMISSION_EDIT_POST, "无权删除帖子");
        }
        post.setStatus("DELETED");
        postRepository.save(post);
    }

    @Transactional
    public Comment addComment(Long clubId, Long postId, Long authorId, String content) {
        if (!isMember(clubId, authorId)) {
            throw new BizException(ErrorCode.NOT_CLUB_MEMBER, "请先加入俱乐部");
        }
        if (sensitiveService.containsSensitive(content)) {
            throw new BizException(ErrorCode.CONTENT_SENSITIVE, "内容包含违规信息");
        }
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setAuthorId(authorId);
        comment.setContent(content);
        comment = commentRepository.save(comment);

        Post post = postRepository.findById(postId).orElseThrow();
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);
        return comment;
    }

    public List<Post> getPosts(Long clubId) {
        return postRepository.findByClubIdAndStatusOrderByPublishTimeDesc(clubId, "ACTIVE");
    }

    public List<Comment> getComments(Long postId) {
        return commentRepository.findByPostIdAndStatusOrderByCreateTimeAsc(postId, "ACTIVE");
    }

    @Transactional
    public void dissolveClub(Long clubId, Long operatorId) {
        if (!isAdmin(clubId, operatorId)) {
            throw new BizException(ErrorCode.NOT_CLUB_MEMBER, "无权操作");
        }
        List<Event> activeEvents = eventRepository.findByClubIdAndStatus(clubId, "IN_PROGRESS");
        if (!activeEvents.isEmpty()) {
            throw new BizException(ErrorCode.CLUB_HAS_ACTIVE_EVENT, "存在进行中赛事，不可解散");
        }
        Club club = getClub(clubId);
        club.setStatus("DISSOLVED");
        clubRepository.save(club);

        List<ClubMember> members = clubMemberRepository.findByClubIdAndStatus(clubId, "ACTIVE");
        members.forEach(m -> notifyService.sendNotification(m.getUserId(), "CLUB_DISSOLVED",
                "俱乐部解散", "俱乐部" + club.getName() + "已解散", clubId));
    }

    public List<Long> getMemberUserIds(Long clubId) {
        return clubMemberRepository.findByClubIdAndStatus(clubId, "ACTIVE").stream()
                .map(ClubMember::getUserId)
                .collect(Collectors.toList());
    }
}