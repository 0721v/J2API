package com.apiplatform.service.impl;

import com.apiplatform.common.PageResult;
import com.apiplatform.entity.ConsumptionRecord;
import com.apiplatform.entity.DailyUserStat;
import com.apiplatform.entity.User;
import com.apiplatform.mapper.ConsumptionRecordMapper;
import com.apiplatform.mapper.DailyUserStatMapper;
import com.apiplatform.mapper.UserMapper;
import com.apiplatform.service.ReportService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报表服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ConsumptionRecordMapper consumptionRecordMapper;
    private final DailyUserStatMapper dailyUserStatMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult<ConsumptionRecord> getConsumptionRecords(Long userId, LocalDate startDate, LocalDate endDate, String type, int page, int pageSize) {
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);

        LambdaQueryWrapper<ConsumptionRecord> wrapper = new LambdaQueryWrapper<ConsumptionRecord>()
                .eq(ConsumptionRecord::getUserId, userId)
                .between(ConsumptionRecord::getCreatedAt, startTime, endTime);

        if (type != null && !type.isEmpty()) {
            wrapper.eq(ConsumptionRecord::getType, type);
        }

        wrapper.orderByDesc(ConsumptionRecord::getCreatedAt);

        IPage<ConsumptionRecord> result = consumptionRecordMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), (long) page, (long) pageSize);
    }

    @Override
    public Map<String, BigDecimal> getConsumptionSummary(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);

        Map<String, BigDecimal> result = consumptionRecordMapper.sumByUserIdAndDateRange(userId, startTime, endTime);

        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("income", result.getOrDefault("income", BigDecimal.ZERO));
        summary.put("expense", result.getOrDefault("expense", BigDecimal.ZERO));
        summary.put("balance", result.getOrDefault("income", BigDecimal.ZERO).subtract(result.getOrDefault("expense", BigDecimal.ZERO)));

        return summary;
    }

    @Override
    public List<Map<String, Object>> getConsumptionByType(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);

        return consumptionRecordMapper.groupByType(userId, startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> getConsumptionTrend(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);

        return consumptionRecordMapper.groupByDay(userId, startTime, endTime);
    }

    @Override
    public PageResult<Map<String, Object>> getDailyStats(Long userId, LocalDate startDate, LocalDate endDate, int page, int pageSize) {
        List<DailyUserStat> stats = dailyUserStatMapper.selectByUserIdAndDateRange(userId, startDate, endDate);

        // 转换为Map列表
        List<Map<String, Object>> records = stats.stream().map(stat -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", stat.getStatDate());
            map.put("totalCalls", stat.getTotalApiCalls());
            map.put("inputTokens", stat.getTotalInputTokens());
            map.put("outputTokens", stat.getTotalOutputTokens());
            map.put("cost", stat.getTotalCost());
            map.put("recharge", stat.getRechargeAmount());
            map.put("reward", stat.getRewardAmount());
            map.put("balance", stat.getBalanceEnd());
            map.put("activeTokens", stat.getActiveTokens());
            map.put("modelUsage", stat.getModelUsage());
            map.put("channelUsage", stat.getChannelUsage());
            return map;
        }).toList();

        long total = stats.size();
        return PageResult.of(records, total, (long) page, (long) pageSize);
    }

    @Override
    public Map<String, Object> getOverallStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        // 累计统计
        Map<String, Object> sumAll = dailyUserStatMapper.sumByUserIdAndDateRange(userId, LocalDate.of(2020, 1, 1), LocalDate.now());
        stats.put("totalCalls", sumAll.getOrDefault("total_calls", 0));
        stats.put("totalInputTokens", sumAll.getOrDefault("total_input_tokens", 0L));
        stats.put("totalOutputTokens", sumAll.getOrDefault("total_output_tokens", 0L));
        stats.put("totalCost", sumAll.getOrDefault("total_cost", BigDecimal.ZERO));
        stats.put("totalRecharge", sumAll.getOrDefault("total_recharge", BigDecimal.ZERO));
        stats.put("totalReward", sumAll.getOrDefault("total_reward", BigDecimal.ZERO));

        // 用户余额
        User user = userMapper.selectById(userId);
        if (user != null) {
            stats.put("balance", user.getBalance());
        }

        return stats;
    }

    @Override
    public Map<String, Object> getMonthStats(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);

        Map<String, Object> stats = getDailyUserStats(userId, startOfMonth, today);
        stats.put("startDate", startOfMonth);
        stats.put("endDate", today);

        return stats;
    }

    @Override
    public Map<String, Object> getTodayStats(Long userId) {
        LocalDate today = LocalDate.now();
        return getDailyUserStats(userId, today, today);
    }

    private Map<String, Object> getDailyUserStats(Long userId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();

        // 从每日统计表获取
        List<DailyUserStat> dailyStats = dailyUserStatMapper.selectByUserIdAndDateRange(userId, startDate, endDate);

        long totalCalls = 0;
        long totalInputTokens = 0;
        long totalOutputTokens = 0;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalRecharge = BigDecimal.ZERO;
        BigDecimal totalReward = BigDecimal.ZERO;

        for (DailyUserStat stat : dailyStats) {
            totalCalls += stat.getTotalApiCalls() != null ? stat.getTotalApiCalls() : 0;
            totalInputTokens += stat.getTotalInputTokens() != null ? stat.getTotalInputTokens() : 0;
            totalOutputTokens += stat.getTotalOutputTokens() != null ? stat.getTotalOutputTokens() : 0;
            totalCost = totalCost.add(stat.getTotalCost() != null ? stat.getTotalCost() : BigDecimal.ZERO);
            totalRecharge = totalRecharge.add(stat.getRechargeAmount() != null ? stat.getRechargeAmount() : BigDecimal.ZERO);
            totalReward = totalReward.add(stat.getRewardAmount() != null ? stat.getRewardAmount() : BigDecimal.ZERO);
        }

        stats.put("totalCalls", totalCalls);
        stats.put("inputTokens", totalInputTokens);
        stats.put("outputTokens", totalOutputTokens);
        stats.put("cost", totalCost);
        stats.put("recharge", totalRecharge);
        stats.put("reward", totalReward);

        return stats;
    }
}
