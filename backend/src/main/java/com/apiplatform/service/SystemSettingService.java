package com.apiplatform.service;

import com.apiplatform.entity.SystemSetting;

import java.util.List;
import java.util.Map;

/**
 * 系统设置服务接口
 */
public interface SystemSettingService {

    /**
     * 获取所有设置
     */
    List<SystemSetting> getAllSettings();

    /**
     * 获取指定类别的设置
     */
    Map<String, String> getSettingsByCategory(String category);

    /**
     * 获取单个设置
     */
    String getSetting(String key);

    /**
     * 获取单个设置（带默认值）
     */
    String getSetting(String key, String defaultValue);

    /**
     * 更新单个设置
     */
    void updateSetting(String key, String value);

    /**
     * 更新单个设置（指定类别）
     */
    void updateSetting(String category, String key, String value);

    /**
     * 批量更新设置
     */
    void updateSettings(Map<String, String> settings);

    /**
     * 删除设置（重置为默认值）
     */
    void deleteSetting(String key);

    /**
     * 初始化默认设置
     */
    void initDefaultSettings();
}
