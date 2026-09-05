package com.pingpong.service;

import com.pingpong.common.BizException;
import com.pingpong.common.ErrorCode;
import com.pingpong.entity.User;
import com.pingpong.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Random;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final SensitiveService sensitiveService;
    private final NotifyService notifyService;

    @Value("${app.init-score:1000}")
    private int initScore;

    public UserService(UserRepository userRepository, SessionService sessionService,
                       SensitiveService sensitiveService, NotifyService notifyService) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
        this.sensitiveService = sensitiveService;
        this.notifyService = notifyService;
    }

    public String sendSmsCode(String phone) {
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new BizException(90001, "手机号格式非法");
        }
        if (!sessionService.checkSmsLimit(phone)) {
            throw new BizException(ErrorCode.SMS_LIMIT_EXCEED, "发送频次超限，请1分钟后再试");
        }
        String code = String.format("%06d", new Random().nextInt(1000000));
        sessionService.saveSmsCode(phone, code);
        return code;
    }

    public String register(String phone, String password, String smsCode) {
        if (userRepository.existsByPhone(phone)) {
            throw new BizException(ErrorCode.PHONE_EXISTS, "手机号已注册");
        }
        String cachedCode = sessionService.getSmsCode(phone);
        if (cachedCode == null || !cachedCode.equals(smsCode)) {
            throw new BizException(ErrorCode.SMS_CODE_ERROR, "验证码错误或过期");
        }
        sessionService.removeSmsCode(phone);

        User user = new User();
        user.setPhone(phone);
        user.setPassword(hashPassword(password));
        user.setCurrentScore(initScore);
        user.setNickname("球友" + phone.substring(phone.length() - 4));
        user = userRepository.save(user);

        return sessionService.createUserToken(user.getId());
    }

    public String login(String phone, String password) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BizException(ErrorCode.ACCOUNT_NOT_FOUND, "账号不存在"));
        if (!hashPassword(password).equals(user.getPassword())) {
            throw new BizException(ErrorCode.PASSWORD_ERROR, "密码错误");
        }
        user.setLastLoginTime(java.time.LocalDateTime.now());
        userRepository.save(user);
        return sessionService.createUserToken(user.getId());
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.USER_NOT_FOUND, "用户不存在"));
    }

    public User updateProfile(Long userId, String nickname, String realName, String gender,
                              String city, String skillLevel) {
        User user = getUserById(userId);
        if (realName != null && (realName.length() < 2 || realName.length() > 20)) {
            throw new BizException(ErrorCode.PROFILE_INVALID, "姓名长度需2-20字符");
        }
        if (nickname != null && sensitiveService.containsSensitive(nickname)) {
            throw new BizException(ErrorCode.NICKNAME_SENSITIVE, "昵称包含违规内容");
        }
        if (skillLevel != null) {
            List<String> validLevels = List.of("BEGINNER", "INTERMEDIATE", "ADVANCED", "PROFESSIONAL");
            if (!validLevels.contains(skillLevel)) {
                throw new BizException(ErrorCode.PROFILE_INVALID, "技术水平等级非法");
            }
        }
        if (nickname != null) user.setNickname(nickname);
        if (realName != null) user.setRealName(realName);
        if (gender != null) user.setGender(gender);
        if (city != null) user.setCity(city);
        if (skillLevel != null) user.setSkillLevel(skillLevel);
        user.setProfileCompleted(true);
        return userRepository.save(user);
    }

    public String updateAvatar(Long userId, String avatarUrl) {
        User user = getUserById(userId);
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
        return avatarUrl;
    }

    public List<User> getNearbyUsers(String city) {
        if (city == null || city.isEmpty()) {
            return userRepository.findAllByOrderByCurrentScoreDescLastMatchTimeDesc();
        }
        return userRepository.findByCityOrderByCurrentScoreDesc(city);
    }

    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}