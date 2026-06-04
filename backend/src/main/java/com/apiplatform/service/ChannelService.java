package com.apiplatform.service;

import com.apiplatform.common.PageResult;
import com.apiplatform.entity.Channel;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 渠道服务接口
 *
 * @author API Platform Team
 */
public interface ChannelService extends IService<Channel> {

    /**
     * 创建渠道
     */
    Channel createChannel(String name, String type, String subType, String endpoint,
                          String apiKey, String authType, String customHeaders,
                          Integer timeout, String modelMapping, String defaultModel,
                          Integer weight, java.math.BigDecimal costPerThousand,
                          Integer priority, Integer maxRetries, Long backupChannelId,
                          String region, String description);

    /**
     * 更新渠道
     */
    Channel updateChannel(Long channelId, String name, String type, String subType,
                          String endpoint, String apiKey, String authType, String customHeaders,
                          Integer timeout, String modelMapping, String defaultModel,
                          Integer weight, java.math.BigDecimal costPerThousand,
                          Integer priority, Integer maxRetries, Long backupChannelId,
                          String region, String description);

    /**
     * 删除渠道
     */
    void deleteChannel(Long channelId);

    /**
     * 启用渠道
     */
    void enableChannel(Long channelId);

    /**
     * 禁用渠道
     */
    void disableChannel(Long channelId);

    /**
     * 标记渠道故障
     */
    void markChannelFailed(Long channelId);

    /**
     * 恢复渠道
     */
    void recoverChannel(Long channelId);

    /**
     * 选择最优渠道（加权随机）
     */
    Channel selectChannel(String modelName, String channelType);

    /**
     * 获取可用渠道列表
     */
    List<Channel> getAvailableChannels(String type);

    /**
     * 获取备用渠道
     */
    Channel getBackupChannel(Long channelId);

    /**
     * 分页查询渠道
     */
    PageResult<Channel> pageChannels(int page, int size, String keyword, String type, String status);

    /**
     * 测试渠道连接
     */
    Map<String, Object> testChannel(Long channelId, String testModel);

    /**
     * 统计渠道使用情况
     */
    List<Map<String, Object>> getChannelUsageStats(Long startTime, Long endTime);

    /**
     * 检查渠道是否可用
     */
    boolean isChannelAvailable(Long channelId);

    /**
     * 根据类型统计渠道数量
     */
    List<Map<String, Object>> countByType();
}
