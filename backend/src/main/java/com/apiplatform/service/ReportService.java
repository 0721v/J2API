package com.apiplatform.service;

import com.apiplatform.common.PageResult;
import com.apiplatform.entity.ConsumptionRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 报表服务接口
 */
public interface ReportService {

    /**
     * 获取消费记录列表
     */
    PageResult<ConsumptionRecord> getConsumptionRecords(Long userId, LocalDate startDate, LocalDate endDate, String type, int page, int pageSize);

    /**
     * 获取消费汇总统计
     */
    Map<String, BigDecimal> getConsumptionSummary(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 按类型分组统计
     */
    List<Map<String, Object>> getConsumptionByType(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 按天分组统计消费趋势
     */
    List<Map<String, Object>> getConsumptionTrend(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取每日统计列表
     */
    PageResult<Map<String, Object>> getDailyStats(Long userId, LocalDate startDate, LocalDate endDate, int page, int pageSize);

    /**
     * 获取整体统计汇总
     */
    Map<String, Object> getOverallStats(Long userId);

    /**
     * 获取本月统计数据
     */
    Map<String, Object> getMonthStats(Long userId);

    /**
     * 获取今日统计数据
     */
    Map<String, Object> getTodayStats(Long userId);
}
