package com.pingpong.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {
    private final Map<String, Long> userSessions = new ConcurrentHashMap<>();
    private final Map<String, Long> adminSessions = new ConcurrentHashMap<>();
    private final Map<String, String> smsCodes = new ConcurrentHashMap<>();
    private final Map<String, Long> smsLimits = new ConcurrentHashMap<>();

    public String createUserToken(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        userSessions.put(token, userId);
        return token;
    }

    public Long getUserId(String token) {
        return token == null ? null : userSessions.get(token);
    }

    public void removeUserToken(String token) {
        userSessions.remove(token);
    }

    public String createAdminToken(Long adminId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        adminSessions.put(token, adminId);
        return token;
    }

    public Long getAdminId(String token) {
        return token == null ? null : adminSessions.get(token);
    }

    public void saveSmsCode(String phone, String code) {
        smsCodes.put(phone, code);
    }

    public String getSmsCode(String phone) {
        return smsCodes.get(phone);
    }

    public void removeSmsCode(String phone) {
        smsCodes.remove(phone);
    }

    public boolean checkSmsLimit(String phone) {
        long now = System.currentTimeMillis();
        Long last = smsLimits.get(phone);
        if (last != null && now - last < 60000) return false;
        smsLimits.put(phone, now);
        return true;
    }
}