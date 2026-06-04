package com.apiplatform.service;

import com.apiplatform.common.PageResult;
import com.apiplatform.entity.Announcement;

import java.util.List;

/**
 * 公告服务接口
 */
public interface AnnouncementService {

    /**
     * 获取已发布的公告列表（分页）
     */
    PageResult<Announcement> getPublishedList(Long userId, int page, int pageSize);

    /**
     * 获取公告详情
     */
    Announcement getById(Long id);

    /**
     * 获取未读公告数量
     */
    int getUnreadCount(Long userId);

    /**
     * 标记公告为已读
     */
    void markAsRead(Long announcementId, Long userId);

    /**
     * 管理员：获取公告列表（分页）
     */
    PageResult<Announcement> getAdminList(int page, int pageSize);

    /**
     * 管理员：创建公告
     */
    Announcement create(Announcement announcement, Long adminId);

    /**
     * 管理员：更新公告
     */
    Announcement update(Announcement announcement);

    /**
     * 管理员：删除公告
     */
    void delete(Long id);

    /**
     * 管理员：发布公告
     */
    Announcement publish(Long id);

    /**
     * 管理员：归档公告
     */
    Announcement archive(Long id);

    /**
     * 管理员：获取历史公告（分页）
     */
    PageResult<Announcement> getHistoryList(int page, int pageSize);

    /**
     * 管理员：增加浏览次数
     */
    void incrementViewCount(Long id);
}
