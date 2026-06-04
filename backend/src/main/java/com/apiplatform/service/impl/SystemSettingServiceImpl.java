package com.apiplatform.service.impl;

import com.apiplatform.entity.SystemSetting;
import com.apiplatform.mapper.SystemSettingMapper;
import com.apiplatform.service.SystemSettingService;
import com.apiplatform.util.CacheUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 系统设置服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemSettingServiceImpl implements SystemSettingService {

    private final SystemSettingMapper systemSettingMapper;
    private final CacheUtil cacheUtil;

    private static final String SETTINGS_CACHE_PREFIX = "system:setting:";
    private static final long CACHE_EXPIRE_HOURS = 24;

    @Override
    public List<SystemSetting> getAllSettings() {
        return systemSettingMapper.selectList(null);
    }

    @Override
    public Map<String, String> getSettingsByCategory(String category) {
        List<SystemSetting> settings = systemSettingMapper.findByCategory(category);
        Map<String, String> result = new HashMap<>();
        for (SystemSetting setting : settings) {
            result.put(setting.getSettingKey(), setting.getSettingValue());
        }
        return result;
    }

    @Override
    public String getSetting(String key) {
        String cacheKey = SETTINGS_CACHE_PREFIX + key;
        String cachedValue = cacheUtil.get(cacheKey);

        if (cachedValue != null) {
            return cachedValue;
        }

        LambdaQueryWrapper<SystemSetting> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemSetting::getSettingKey, key);
        SystemSetting setting = systemSettingMapper.selectOne(queryWrapper);

        if (setting != null && setting.getSettingValue() != null) {
            String value = setting.getSettingValue();
            cacheUtil.set(cacheKey, value, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            return value;
        }

        return null;
    }

    @Override
    public String getSetting(String key, String defaultValue) {
        String value = getSetting(key);
        return value != null ? value : defaultValue;
    }

    @Override
    @Transactional
    public void updateSetting(String key, String value) {
        LambdaUpdateWrapper<SystemSetting> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SystemSetting::getSettingKey, key)
                .set(SystemSetting::getSettingValue, value)
                .set(SystemSetting::getUpdatedAt, LocalDateTime.now());
        systemSettingMapper.update(null, updateWrapper);

        // 清除缓存
        String cacheKey = SETTINGS_CACHE_PREFIX + key;
        cacheUtil.delete(cacheKey);

        log.info("系统设置已更新: {} = {}", key, value);
    }

    @Override
    @Transactional
    public void updateSetting(String category, String key, String value) {
        SystemSetting setting = systemSettingMapper.findByCategoryAndKey(category, key);

        if (setting != null) {
            setting.setSettingValue(value);
            setting.setUpdatedAt(LocalDateTime.now());
            systemSettingMapper.updateById(setting);
        } else {
            // 创建新设置
            setting = new SystemSetting();
            setting.setCategory(category);
            setting.setSettingKey(key);
            setting.setSettingValue(value);
            setting.setCreatedAt(LocalDateTime.now());
            setting.setUpdatedAt(LocalDateTime.now());
            systemSettingMapper.insert(setting);
        }

        // 清除缓存
        String cacheKey = SETTINGS_CACHE_PREFIX + key;
        cacheUtil.delete(cacheKey);

        log.info("系统设置已更新: {}/{} = {}", category, key, value);
    }

    @Override
    @Transactional
    public void updateSettings(Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            updateSetting(entry.getKey(), entry.getValue());
        }
    }

    @Override
    @Transactional
    public void deleteSetting(String key) {
        LambdaQueryWrapper<SystemSetting> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemSetting::getSettingKey, key);
        SystemSetting setting = systemSettingMapper.selectOne(queryWrapper);

        if (setting != null) {
            // 重置为 null
            setting.setSettingValue(null);
            setting.setUpdatedAt(LocalDateTime.now());
            systemSettingMapper.updateById(setting);

            // 清除缓存
            String cacheKey = SETTINGS_CACHE_PREFIX + key;
            cacheUtil.delete(cacheKey);

            log.info("系统设置已重置: {}", key);
        }
    }

    @Override
    public void initDefaultSettings() {
        // 初始化默认设置
        initCustomizationSettings();
        initSeoSettings();
        initRegistrationSettings();
        initQuotaSettings();
        initProxySettings();
    }

    private void initCustomizationSettings() {
        initSetting("customization", "site_name", "API Platform", "string", "网站名称", 1);
        initSetting("customization", "site_title", "API Platform - 您的API聚合平台", "string", "网站标题", 2);
        initSetting("customization", "site_description", "提供最优质的AI API服务", "string", "网站描述", 3);
        initSetting("customization", "site_logo", "", "string", "网站Logo", 4);
        initSetting("customization", "favicon", "", "string", "网站图标", 5);
        initSetting("customization", "default_theme", "light", "string", "默认主题", 6);
        initSetting("customization", "primary_color", "#409eff", "string", "主题色", 7);
        initSetting("customization", "available_themes", "light,dark", "string", "可用主题", 8);
        initSetting("customization", "copyright", "", "string", "版权信息", 9);
        initSetting("customization", "icp", "", "string", "备案号", 10);
        initSetting("customization", "contact_email", "", "string", "联系方式", 11);
        initSetting("customization", "social_links", "{}", "json", "社交链接", 12);
        initSetting("customization", "show_models", "true", "boolean", "显示模型列表", 13);
        initSetting("customization", "show_pricing", "true", "boolean", "显示价格表", 14);
        initSetting("customization", "show_usage", "true", "boolean", "显示用量统计", 15);
        initSetting("customization", "show_chat", "true", "boolean", "显示Chat入口", 16);
        initSetting("customization", "custom_css", "", "string", "自定义CSS", 17);
        initSetting("customization", "custom_js", "", "string", "自定义JS", 18);
    }

    private void initSeoSettings() {
        initSetting("seo", "meta_title", "API Platform", "string", "Meta Title", 1);
        initSetting("seo", "meta_keywords", "API, AI, GPT, Claude, OpenAI", "string", "Meta Keywords", 2);
        initSetting("seo", "meta_description", "API Platform 提供最优质的AI API服务", "string", "Meta Description", 3);
        initSetting("seo", "og_title", "API Platform", "string", "OG Title", 4);
        initSetting("seo", "og_description", "API Platform 提供最优质的AI API服务", "string", "OG Description", 5);
        initSetting("seo", "og_image", "", "string", "OG Image", 6);
        initSetting("seo", "enable_sitemap", "true", "boolean", "启用站点地图", 7);
        initSetting("seo", "enable_robots", "true", "boolean", "启用Robots.txt", 8);
        initSetting("seo", "custom_robots", "", "string", "自定义Robots", 9);
        initSetting("seo", "canonical_url", "", "string", "Canonical URL", 10);
    }

    private void initRegistrationSettings() {
        initSetting("registration", "allow_register", "true", "boolean", "允许注册", 1);
        initSetting("registration", "require_email_verification", "false", "boolean", "邮箱验证", 2);
        initSetting("registration", "require_captcha", "false", "boolean", "验证码", 3);
        initSetting("registration", "default_group_id", "1", "number", "默认用户组", 4);
        initSetting("registration", "terms_of_service", "", "string", "服务条款", 5);
        initSetting("registration", "privacy_policy", "", "string", "隐私政策", 6);
        initSetting("registration", "enable_invite", "false", "boolean", "邀请注册", 7);
        initSetting("registration", "invite_bonus", "0", "number", "邀请奖励", 8);
    }

    private void initQuotaSettings() {
        initSetting("quota", "new_user_bonus", "0", "number", "新用户赠送积分", 1);
        initSetting("quota", "free_minute_limit", "60", "number", "免费用户分钟限制", 2);
        initSetting("quota", "free_daily_limit", "1000", "number", "免费用户每日限制", 3);
        initSetting("quota", "free_monthly_limit", "10000", "number", "免费用户每月限制", 4);
    }

    private void initProxySettings() {
        initSetting("proxy", "enable_proxy", "true", "boolean", "启用代理功能", 1);
        initSetting("proxy", "enable_custom_endpoint", "true", "boolean", "自定义端点", 2);
        initSetting("proxy", "allowed_domains", "", "string", "允许的域名", 3);
        initSetting("proxy", "blocked_domains", "", "string", "禁止的域名", 4);
        initSetting("proxy", "default_timeout", "60000", "number", "默认超时(ms)", 5);
        initSetting("proxy", "max_timeout", "120000", "number", "最大超时(ms)", 6);
        initSetting("proxy", "enable_request_log", "true", "boolean", "请求日志", 7);
        initSetting("proxy", "log_retention_days", "30", "number", "日志保留天数", 8);
        initSetting("proxy", "enable_signature", "false", "boolean", "请求签名", 9);
        initSetting("proxy", "signature_secret", "", "string", "签名密钥", 10);
        initSetting("proxy", "rate_limit_per_minute", "60", "number", "每分钟请求数", 11);
        initSetting("proxy", "rate_limit_per_hour", "1000", "number", "每小时请求数", 12);
        initSetting("proxy", "rate_limit_per_day", "10000", "number", "每天请求数", 13);
    }

    private void initSetting(String category, String key, String value, String type, String displayName, int sortOrder) {
        SystemSetting setting = systemSettingMapper.findByCategoryAndKey(category, key);
        if (setting == null) {
            setting = new SystemSetting();
            setting.setCategory(category);
            setting.setSettingKey(key);
            setting.setSettingValue(value);
            setting.setValueType(type);
            setting.setDescription(displayName);
            setting.setSortOrder(sortOrder);
            setting.setCreatedAt(LocalDateTime.now());
            setting.setUpdatedAt(LocalDateTime.now());
            systemSettingMapper.insert(setting);
        }
    }
}
