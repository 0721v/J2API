package com.apiplatform.mapper;

import com.apiplatform.entity.AnnouncementRead;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 公告阅读记录Mapper
 */
@Mapper
public interface AnnouncementReadMapper extends BaseMapper<AnnouncementRead> {

    /**
     * 检查用户是否已阅读公告
     */
    @Select("SELECT COUNT(*) > 0 FROM announcement_reads WHERE announcement_id = #{announcementId} AND user_id = #{userId}")
    boolean existsByAnnouncementAndUser(@Param("announcementId") Long announcementId, @Param("userId") Long userId);
}
