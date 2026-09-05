package com.pingpong.service;

import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;

@Service
public class SensitiveService {
    private final List<String> sensitiveWords = Arrays.asList("垃圾", "废物", "傻逼", "操", "fuck", "shit");

    public boolean containsSensitive(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        return sensitiveWords.stream().anyMatch(lower::contains);
    }
}