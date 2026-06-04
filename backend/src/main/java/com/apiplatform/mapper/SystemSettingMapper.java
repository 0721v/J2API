package com.apiplatform.mapper;

import com.apiplatform.entity.SystemSetting;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统设置 Mapper
 */
@Mapper
public interface SystemSettingMapper extends BaseMapper<SystemSetting> {

    /**
     * 根据类别获取所有设置
     */
    List<SystemSetting> findByCategory(@Param("category") String category);

    /**
     * 根据类别和键获取设置
     */
    SystemSetting findByCategoryAndKey(@Param("category") String category, @Param("settingKey") String settingKey);

    /**
     * 批量更新设置
     */
    int updateByCategoryAndKey(@Param("category") String category, @Param("settingKey") String settingKey, @Param("value") String value);
}
