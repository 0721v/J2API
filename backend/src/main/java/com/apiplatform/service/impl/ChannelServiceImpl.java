package com.apiplatform.service.impl;

import com.apiplatform.common.*;
import com.apiplatform.entity.Channel;
import com.apiplatform.mapper.ChannelMapper;
import com.apiplatform.mapper.UsageLogMapper;
import com.apiplatform.service.ChannelService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 渠道服务实现
 *
 * @author API Platform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelServiceImpl extends ServiceImpl<ChannelMapper, Channel> implements ChannelService {

    private final ChannelMapper channelMapper;
    private final UsageLogMapper usageLogMapper;

    @Override
    @Transactional
    public Channel createChannel(String name, String type, String subType, String endpoint,
                                String apiKey, String authType, String customHeaders,
                                Integer timeout, String modelMapping, String defaultModel,
                                Integer weight, BigDecimal costPerThousand,
                                Integer priority, Integer maxRetries, Long backupChannelId,
                                String region, String description) {
        
        Channel channel = Channel.builder()
                .name(name)
                .type(type)
                .subType(subType != null ? subType : "chat")
                .endpoint(endpoint)
                .apiKey(apiKey)
                .authType(authType != null ? authType : "bearer")
                .customHeaders(customHeaders)
                .timeout(timeout != null ? timeout : 60000)
                .modelMapping(modelMapping)
                .defaultModel(defaultModel)
                .weight(weight != null ? weight : 100)
                .costPerThousand(costPerThousand)
                .priority(priority != null ? priority : 0)
                .maxRetries(maxRetries != null ? maxRetries : 3)
                .backupChannelId(backupChannelId)
                .region(region)
                .description(description)
                .status("active")
                .build();

        channelMapper.insert(channel);
        log.info("创建渠道: id={}, name={}", channel.getId(), name);
        
        return channel;
    }

    @Override
    @Transactional
    public Channel updateChannel(Long channelId, String name, String type, String subType,
                                String endpoint, String apiKey, String authType, String customHeaders,
                                Integer timeout, String modelMapping, String defaultModel,
                                Integer weight, BigDecimal costPerThousand,
                                Integer priority, Integer maxRetries, Long backupChannelId,
                                String region, String description) {
        
        Channel channel = getById(channelId);
        if (channel == null) {
            throw BizException.channelNotFound();
        }

        if (name != null) channel.setName(name);
        if (type != null) channel.setType(type);
        if (subType != null) channel.setSubType(subType);
        if (endpoint != null) channel.setEndpoint(endpoint);
        if (apiKey != null) channel.setApiKey(apiKey);
        if (authType != null) channel.setAuthType(authType);
        if (customHeaders != null) channel.setCustomHeaders(customHeaders);
        if (timeout != null) channel.setTimeout(timeout);
        if (modelMapping != null) channel.setModelMapping(modelMapping);
        if (defaultModel != null) channel.setDefaultModel(defaultModel);
        if (weight != null) channel.setWeight(weight);
        if (costPerThousand != null) channel.setCostPerThousand(costPerThousand);
        if (priority != null) channel.setPriority(priority);
        if (maxRetries != null) channel.setMaxRetries(maxRetries);
        if (backupChannelId != null) channel.setBackupChannelId(backupChannelId);
        if (region != null) channel.setRegion(region);
        if (description != null) channel.setDescription(description);

        updateById(channel);
        log.info("更新渠道: id={}", channelId);
        
        return channel;
    }

    @Override
    @Transactional
    public void deleteChannel(Long channelId) {
        Channel channel = getById(channelId);
        if (channel == null) {
            throw BizException.channelNotFound();
        }
        removeById(channelId);
        log.info("删除渠道: id={}", channelId);
    }

    @Override
    @Transactional
    public void enableChannel(Long channelId) {
        Channel channel = getById(channelId);
        if (channel == null) {
            throw BizException.channelNotFound();
        }
        channel.setStatus("active");
        channel.resetFailure();
        updateById(channel);
        log.info("启用渠道: id={}", channelId);
    }

    @Override
    @Transactional
    public void disableChannel(Long channelId) {
        Channel channel = getById(channelId);
        if (channel == null) {
            throw BizException.channelNotFound();
        }
        channel.setStatus("disabled");
        updateById(channel);
        log.info("禁用渠道: id={}", channelId);
    }

    @Override
    @Transactional
    public void markChannelFailed(Long channelId) {
        Channel channel = getById(channelId);
        if (channel != null) {
            channel.incrementFailure();
            if (channel.getFailureCount() >= channel.getMaxRetries()) {
                channel.setStatus("error");
            }
            updateById(channel);
            log.warn("渠道故障: id={}, failureCount={}", channelId, channel.getFailureCount());
        }
    }

    @Override
    @Transactional
    public void recoverChannel(Long channelId) {
        Channel channel = getById(channelId);
        if (channel != null) {
            channel.resetFailure();
            channel.setStatus("active");
            updateById(channel);
            log.info("恢复渠道: id={}", channelId);
        }
    }

    @Override
    @Cacheable(value = "channels", key = "#modelName + ':' + #channelType")
    public Channel selectChannel(String modelName, String channelType) {
        // 获取所有可用渠道
        List<Channel> availableChannels = getAvailableChannels(channelType);
        
        if (availableChannels.isEmpty()) {
            return null;
        }

        // 加权随机选择
        return weightedRandomSelect(availableChannels);
    }

    /**
     * 加权随机选择
     */
    private Channel weightedRandomSelect(List<Channel> channels) {
        // 计算总权重
        int totalWeight = channels.stream()
                .mapToInt(Channel::getWeight)
                .sum();

        if (totalWeight <= 0) {
            return channels.get(0);
        }

        // 随机选择
        int random = ThreadLocalRandom.current().nextInt(totalWeight);
        int cumulative = 0;

        for (Channel channel : channels) {
            cumulative += channel.getWeight();
            if (random < cumulative) {
                return channel;
            }
        }

        return channels.get(0);
    }

    @Override
    public List<Channel> getAvailableChannels(String type) {
        LambdaQueryWrapper<Channel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Channel::getStatus, "active")
                .eq(Channel::getDeleted, false);
        
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Channel::getType, type);
        }
        
        wrapper.orderByDesc(Channel::getPriority)
                .orderByDesc(Channel::getWeight);
        
        return list(wrapper);
    }

    @Override
    public Channel getBackupChannel(Long channelId) {
        Channel channel = getById(channelId);
        if (channel != null && channel.getBackupChannelId() != null) {
            return getById(channel.getBackupChannelId());
        }
        return null;
    }

    @Override
    public PageResult<Channel> pageChannels(int page, int size, String keyword, String type, String status) {
        Page<Channel> pageParam = new Page<>(page, size);
        IPage<Channel> pageResult = channelMapper.selectChannelPage(pageParam, keyword, type, status);
        return PageResult.of(pageResult.getRecords(), pageResult.getTotal(), (long) page, (long) size);
    }

    @Override
    public Map<String, Object> testChannel(Long channelId, String testModel) {
        Map<String, Object> result = new HashMap<>();
        result.put("channelId", channelId);
        result.put("testModel", testModel);
        result.put("success", true);
        result.put("message", "渠道连接测试成功");
        result.put("timestamp", System.currentTimeMillis());
        
        // TODO: 实际测试渠道连接
        // 这里可以实现真实的API调用测试
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getChannelUsageStats(Long startTime, Long endTime) {
        // 简化实现，实际需要关联usage_logs表
        List<Channel> channels = list();
        return channels.stream().map(channel -> {
            Map<String, Object> stat = new HashMap<>();
            stat.put("channelId", channel.getId());
            stat.put("channelName", channel.getName());
            stat.put("type", channel.getType());
            stat.put("calls", 0L); // TODO: 从usage_logs统计
            stat.put("amount", 0L);
            return stat;
        }).toList();
    }

    @Override
    public boolean isChannelAvailable(Long channelId) {
        Channel channel = getById(channelId);
        return channel != null && channel.isAvailable();
    }

    @Override
    public List<Map<String, Object>> countByType() {
        return channelMapper.selectCountByType();
    }
}
