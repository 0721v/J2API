package com.apiplatform.service.impl;

import com.apiplatform.common.BizException;
import com.apiplatform.common.PageResult;
import com.apiplatform.entity.Announcement;
import com.apiplatform.entity.AnnouncementRead;
import com.apiplatform.mapper.AnnouncementMapper;
import com.apiplatform.mapper.AnnouncementReadMapper;
import com.apiplatform.service.AnnouncementService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;
    private final AnnouncementReadMapper announcementReadMapper;

    @Override
    public PageResult<Announcement> getPublishedList(Long userId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Announcement> records = announcementMapper.selectPublishedList(LocalDateTime.now(), pageSize, offset);
        long total = countPublishedTotal();

        // 标记哪些已读
        for (Announcement record : records) {
            record.setRead(existsRead(record.getId(), userId));
        }

        return PageResult.of(records, total, page, pageSize);
    }

    @Override
    public Announcement getById(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw BizException.notFound("公告不存在");
        }
        // 增加浏览次数
        incrementViewCount(id);
        return announcement;
    }

    @Override
    public int getUnreadCount(Long userId) {
        return announcementMapper.countUnreadAnnouncements(userId, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void markAsRead(Long announcementId, Long userId) {
        if (!existsRead(announcementId, userId)) {
            AnnouncementRead read = new AnnouncementRead();
            read.setAnnouncementId(announcementId);
            read.setUserId(userId);
            read.setReadAt(LocalDateTime.now());
            announcementReadMapper.insert(read);
        }
    }

    @Override
    public PageResult<Announcement> getAdminList(int page, int pageSize) {
        IPage<Announcement> result = announcementMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Announcement>()
                        .orderByDesc(Announcement::getPriority)
                        .orderByDesc(Announcement::getCreatedAt)
        );
        return PageResult.of(result.getRecords(), result.getTotal(), page, pageSize);
    }

    @Override
    public Announcement create(Announcement announcement, Long adminId) {
        announcement.setCreatedBy(adminId);
        announcement.setViewCount(0);
        if (announcement.getStatus() == null) {
            announcement.setStatus("draft");
        }
        announcementMapper.insert(announcement);
        return announcement;
    }

    @Override
    public Announcement update(Announcement announcement) {
        Announcement exist = announcementMapper.selectById(announcement.getId());
        if (exist == null) {
            throw BizException.notFound("公告不存在");
        }
        announcementMapper.updateById(announcement);
        return announcementMapper.selectById(announcement.getId());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        announcementMapper.deleteById(id);
        // 同时删除阅读记录
        announcementReadMapper.delete(new LambdaQueryWrapper<AnnouncementRead>()
                .eq(AnnouncementRead::getAnnouncementId, id));
    }

    @Override
    public Announcement publish(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw BizException.notFound("公告不存在");
        }
        announcement.setStatus("published");
        announcement.setPublishedAt(LocalDateTime.now());
        announcementMapper.updateById(announcement);
        return announcement;
    }

    @Override
    public Announcement archive(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw BizException.notFound("公告不存在");
        }
        announcement.setStatus("archived");
        announcementMapper.updateById(announcement);
        return announcement;
    }

    @Override
    public PageResult<Announcement> getHistoryList(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Announcement> records = announcementMapper.selectHistoryList(LocalDateTime.now(), pageSize, offset);
        long total = countHistoryTotal();
        return PageResult.of(records, total, page, pageSize);
    }

    @Override
    public void incrementViewCount(Long id) {
        announcementMapper.update(null, new LambdaUpdateWrapper<Announcement>()
                .eq(Announcement::getId, id)
                .setSql("view_count = view_count + 1"));
    }

    private boolean existsRead(Long announcementId, Long userId) {
        return announcementReadMapper.existsByAnnouncementAndUser(announcementId, userId);
    }

    private long countPublishedTotal() {
        return announcementMapper.selectCount(new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, "published")
                .and(w -> w.isNull(Announcement::getExpiredAt)
                        .or()
                        .gt(Announcement::getExpiredAt, LocalDateTime.now()))
                .and(w -> w.eq(Announcement::getTargetType, "all")
                        .or()
                        .eq(Announcement::getTargetType, "all_users")));
    }

    private long countHistoryTotal() {
        return announcementMapper.selectCount(new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, "archived")
                .or()
                .eq(Announcement::getStatus, "published")
                .lt(Announcement::getExpiredAt, LocalDateTime.now()));
    }
}
