package com.apiplatform.mapper;

import com.apiplatform.entity.Announcement;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告Mapper
 */
@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {

    /**
     * 获取已发布的公告列表（分页）
     */
    @Select("SELECT * FROM announcements WHERE status = 'published' " +
            "AND (expired_at IS NULL OR expired_at > #{now}) " +
            "AND (target_type = 'all' OR target_type = 'all_users') " +
            "ORDER BY priority DESC, published_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<Announcement> selectPublishedList(@Param("now") LocalDateTime now, @Param("limit") int limit, @Param("offset") int offset);

    /**
     * 获取未读的公告数量
     */
    @Select("SELECT COUNT(*) FROM announcements a " +
            "LEFT JOIN announcement_reads ar ON a.id = ar.announcement_id AND ar.user_id = #{userId} " +
            "WHERE a.status = 'published' " +
            "AND (a.expired_at IS NULL OR a.expired_at > #{now}) " +
            "AND ar.id IS NULL " +
            "AND (a.target_type = 'all' OR a.target_type = 'all_users')")
    int countUnreadAnnouncements(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * 获取管理员公告列表（分页）
     */
    @Select("SELECT * FROM announcements ORDER BY priority DESC, created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<Announcement> selectAdminList(@Param("limit") int limit, @Param("offset") int offset);

    /**
     * 获取历史公告列表
     */
    @Select("SELECT * FROM announcements WHERE status = 'archived' OR (status = 'published' AND expired_at < #{now}) " +
            "ORDER BY published_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<Announcement> selectHistoryList(@Param("now") LocalDateTime now, @Param("limit") int limit, @Param("offset") int offset);
}
