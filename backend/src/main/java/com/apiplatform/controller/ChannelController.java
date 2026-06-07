package com.apiplatform.controller;

import com.apiplatform.common.BizException;
import com.apiplatform.common.PageResult;
import com.apiplatform.common.Result;
import com.apiplatform.entity.Channel;
import com.apiplatform.service.ChannelService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 渠道控制器
 *
 * @author API Platform Team
 */
@Slf4j
@RestController
@RequestMapping("/admin/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    /**
     * 获取渠道列表
     */
    @GetMapping
    public Result<PageResult<Channel>> getChannels(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        
        PageResult<Channel> result = channelService.pageChannels(page, size, keyword, type, status);
        return Result.success(result);
    }

    /**
     * 获取所有可用渠道
     */
    @GetMapping("/available")
    public Result<List<Channel>> getAvailableChannels(
            @RequestParam(required = false) String type) {
        List<Channel> channels;
        if (type != null && !type.isEmpty()) {
            channels = channelService.getAvailableChannels(type);
        } else {
            channels = channelService.list();
        }
        return Result.success(channels);
    }

    /**
     * 创建渠道
     */
    @PostMapping
    public Result<Channel> createChannel(@Validated @RequestBody CreateChannelRequest request) {
        try {
            Channel channel = channelService.createChannel(
                    request.getName(),
                    request.getType(),
                    request.getSubType(),
                    request.getEndpoint(),
                    request.getApiKey(),
                    request.getAuthType(),
                    request.getCustomHeaders(),
                    request.getTimeout(),
                    request.getModelMapping(),
                    request.getDefaultModel(),
                    request.getWeight(),
                    request.getCostPerThousand(),
                    request.getPriority(),
                    request.getMaxRetries(),
                    request.getBackupChannelId(),
                    request.getRegion(),
                    request.getDescription()
            );
            return Result.success("渠道创建成功", channel);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 更新渠道
     */
    @PutMapping("/{id}")
    public Result<Channel> updateChannel(
            @PathVariable Long id,
            @Validated @RequestBody UpdateChannelRequest request) {
        try {
            Channel channel = channelService.updateChannel(
                    id,
                    request.getName(),
                    request.getType(),
                    request.getSubType(),
                    request.getEndpoint(),
                    request.getApiKey(),
                    request.getAuthType(),
                    request.getCustomHeaders(),
                    request.getTimeout(),
                    request.getModelMapping(),
                    request.getDefaultModel(),
                    request.getWeight(),
                    request.getCostPerThousand(),
                    request.getPriority(),
                    request.getMaxRetries(),
                    request.getBackupChannelId(),
                    request.getRegion(),
                    request.getDescription()
            );
            return Result.success("渠道更新成功", channel);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 删除渠道
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteChannel(@PathVariable Long id) {
        try {
            channelService.deleteChannel(id);
            return Result.success("渠道删除成功", null);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 启用渠道
     */
    @PostMapping("/{id}/enable")
    public Result<Void> enableChannel(@PathVariable Long id) {
        channelService.enableChannel(id);
        return Result.success("渠道已启用", null);
    }

    /**
     * 禁用渠道
     */
    @PostMapping("/{id}/disable")
    public Result<Void> disableChannel(@PathVariable Long id) {
        channelService.disableChannel(id);
        return Result.success("渠道已禁用", null);
    }

    /**
     * 测试渠道连接
     */
    @PostMapping("/{id}/test")
    public Result<Map<String, Object>> testChannel(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "gpt-3.5-turbo") String testModel) {
        try {
            Map<String, Object> result = channelService.testChannel(id, testModel);
            return Result.success("测试完成", result);
        } catch (BizException e) {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 获取渠道使用统计
     */
    @GetMapping("/{id}/stats")
    public Result<Object> getChannelStats(
            @PathVariable Long id,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime) {
        List<Map<String, Object>> stats = channelService.getChannelUsageStats(startTime, endTime);
        return Result.success(stats);
    }

    /**
     * 按类型统计渠�?     */
    @GetMapping("/count-by-type")
    public Result<List<Map<String, Object>>> countByType() {
        List<Map<String, Object>> result = channelService.countByType();
        return Result.success(result);
    }

    // ==================== 请求DTO ====================

    @Data
    public static class CreateChannelRequest {
        private String name;
        private String type;
        private String subType = "chat";
        private String endpoint;
        private String apiKey;
        private String authType = "bearer";
        private String customHeaders;
        private Integer timeout = 60000;
        private String modelMapping;
        private String defaultModel;
        private Integer weight = 100;
        private BigDecimal costPerThousand = BigDecimal.ZERO;
        private Integer priority = 0;
        private Integer maxRetries = 3;
        private Long backupChannelId;
        private String region;
        private String description;
    }

    @Data
    public static class UpdateChannelRequest {
        private String name;
        private String type;
        private String subType;
        private String endpoint;
        private String apiKey;
        private String authType;
        private String customHeaders;
        private Integer timeout;
        private String modelMapping;
        private String defaultModel;
        private Integer weight;
        private BigDecimal costPerThousand;
        private Integer priority;
        private Integer maxRetries;
        private Long backupChannelId;
        private String region;
        private String description;
    }
}
