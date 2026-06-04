package com.apiplatform.util;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * API Key生成工具
 *
 * @author API Platform Team
 */
@Slf4j
@Component
public class ApiKeyUtil {

    @Value("${system.token.prefix:sk-}")
    private String tokenPrefix;

    @Value("${system.token.length:32}")
    private Integer tokenLength;

    private static final String UPPER_CASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER_CASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String ALL_CHARS = UPPER_CASE + LOWER_CASE + DIGITS;

    private final SecureRandom random = new SecureRandom();

    /**
     * 生成API Key
     * 格式: sk-{32位随机字符}
     */
    public String generateApiKey() {
        String randomPart = generateRandomString(tokenLength);
        return tokenPrefix + randomPart;
    }

    /**
     * 生成纯数字API Key
     */
    public String generateNumericKey() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokenLength; i++) {
            sb.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        }
        return tokenPrefix + sb.toString();
    }

    /**
     * 生成带连字符的API Key
     * 格式: sk-{8位}-{8位}-{8位}-{8位}
     */
    public String generateFormattedApiKey() {
        String part1 = generateRandomString(8);
        String part2 = generateRandomString(8);
        String part3 = generateRandomString(8);
        String part4 = generateRandomString(8);
        return String.format("%s%s-%s-%s-%s", tokenPrefix, part1, part2, part3, part4);
    }

    /**
     * 生成随机字符串
     */
    public String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * 生成UUID
     */
    public String generateUUID() {
        return IdUtil.fastUUID().toString(true);
    }

    /**
     * 生成雪花ID
     */
    public Long generateSnowflakeId() {
        return IdUtil.getSnowflakeNextId();
    }

    /**
     * 生成Base64编码的随机密钥
     */
    public String generateBase64Key() {
        byte[] bytes = new byte[tokenLength];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 生成API Secret（用于WebSocket认证等）
     */
    public String generateApiSecret() {
        return "sk_secret_" + generateRandomString(48);
    }

    /**
     * 验证API Key格式
     */
    public boolean validateApiKeyFormat(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return false;
        }
        // 检查是否以sk-开头
        if (!apiKey.startsWith(tokenPrefix)) {
            return false;
        }
        // 检查长度
        String randomPart = apiKey.substring(tokenPrefix.length());
        return randomPart.length() >= 16 && randomPart.length() <= 64;
    }

    /**
     * 脱敏API Key
     */
    public String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 10) {
            return "****";
        }
        int visibleLength = 4;
        return apiKey.substring(0, visibleLength + tokenPrefix.length()) + 
               "****" + 
               apiKey.substring(apiKey.length() - visibleLength);
    }
}
