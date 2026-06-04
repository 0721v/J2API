package com.apiplatform.controller;

import com.apiplatform.entity.SystemSetting;
import com.apiplatform.service.SystemSettingService;
import com.apiplatform.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统设置控制器
 */
@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SystemSettingService systemSettingService;

    /**
     * 获取所有系统设置
     */
    @GetMapping("/system")
    public Result<Map<String, String>> getSystemSettings() {
        List<SystemSetting> settings = systemSettingService.getAllSettings();
        Map<String, String> result = new HashMap<>();
        for (SystemSetting setting : settings) {
            result.put(setting.getSettingKey(), setting.getSettingValue());
        }
        return Result.success(result);
    }

    /**
     * 更新系统设置
     */
    @PutMapping("/system")
    public Result<Void> updateSystemSettings(@RequestBody Map<String, String> settings) {
        systemSettingService.updateSettings(settings);
        return Result.success();
    }

    /**
     * 获取指定类别的设置
     */
    @GetMapping("/category/{category}")
    public Result<Map<String, String>> getSettingsByCategory(@PathVariable String category) {
        Map<String, String> settings = systemSettingService.getSettingsByCategory(category);
        return Result.success(settings);
    }

    /**
     * 更新单个设置
     */
    @PutMapping("/{key}")
    public Result<Void> updateSetting(@PathVariable String key, @RequestBody Map<String, String> body) {
        String value = body.get("value");
        systemSettingService.updateSetting(key, value);
        return Result.success();
    }

    /**
     * 删除设置（重置为默认值）
     */
    @DeleteMapping("/{key}")
    public Result<Void> resetSetting(@PathVariable String key) {
        systemSettingService.deleteSetting(key);
        return Result.success();
    }

    // ==================== 网站自定义设置 ====================

    /**
     * 获取网站自定义设置
     */
    @GetMapping("/customization")
    public Result<Map<String, String>> getCustomizationSettings() {
        Map<String, String> settings = systemSettingService.getSettingsByCategory("customization");
        return Result.success(settings);
    }

    /**
     * 更新网站自定义设置
     */
    @PutMapping("/customization")
    public Result<Void> updateCustomizationSettings(@RequestBody Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            systemSettingService.updateSetting("customization", entry.getKey(), entry.getValue());
        }
        return Result.success();
    }

    // ==================== SEO 设置 ====================

    /**
     * 获取 SEO 设置
     */
    @GetMapping("/seo")
    public Result<Map<String, String>> getSeoSettings() {
        Map<String, String> settings = systemSettingService.getSettingsByCategory("seo");
        return Result.success(settings);
    }

    /**
     * 更新 SEO 设置
     */
    @PutMapping("/seo")
    public Result<Void> updateSeoSettings(@RequestBody Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            systemSettingService.updateSetting("seo", entry.getKey(), entry.getValue());
        }
        return Result.success();
    }

    // ==================== 注册设置 ====================

    /**
     * 获取注册设置
     */
    @GetMapping("/registration")
    public Result<Map<String, String>> getRegistrationSettings() {
        Map<String, String> settings = systemSettingService.getSettingsByCategory("registration");
        return Result.success(settings);
    }

    /**
     * 更新注册设置
     */
    @PutMapping("/registration")
    public Result<Void> updateRegistrationSettings(@RequestBody Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            systemSettingService.updateSetting("registration", entry.getKey(), entry.getValue());
        }
        return Result.success();
    }

    // ==================== 配额设置 ====================

    /**
     * 获取配额设置
     */
    @GetMapping("/quota")
    public Result<Map<String, String>> getQuotaSettings() {
        Map<String, String> settings = systemSettingService.getSettingsByCategory("quota");
        return Result.success(settings);
    }

    /**
     * 更新配额设置
     */
    @PutMapping("/quota")
    public Result<Void> updateQuotaSettings(@RequestBody Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            systemSettingService.updateSetting("quota", entry.getKey(), entry.getValue());
        }
        return Result.success();
    }

    // ==================== 代理设置 ====================

    /**
     * 获取代理设置
     */
    @GetMapping("/proxy")
    public Result<Map<String, String>> getProxySettings() {
        Map<String, String> settings = systemSettingService.getSettingsByCategory("proxy");
        return Result.success(settings);
    }

    /**
     * 更新代理设置
     */
    @PutMapping("/proxy")
    public Result<Void> updateProxySettings(@RequestBody Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            systemSettingService.updateSetting("proxy", entry.getKey(), entry.getValue());
        }
        return Result.success();
    }
}
