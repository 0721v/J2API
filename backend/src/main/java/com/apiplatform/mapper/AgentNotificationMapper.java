package com.apiplatform.mapper;

import com.apiplatform.entity.AgentNotification;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 代理商通知 Mapper
 */
@Mapper
public interface AgentNotificationMapper extends BaseMapper<AgentNotification> {

    /**
     * 分页查询代理商通知
     */
    IPage<AgentNotification> selectByAgentId(Page<AgentNotification> page, @Param("agentId") Long agentId);

    /**
     * 统计未读通知数
     */
    int countUnread(@Param("agentId") Long agentId);

    /**
     * 标记全部已读
     */
    int markAllRead(@Param("agentId") Long agentId);
}
