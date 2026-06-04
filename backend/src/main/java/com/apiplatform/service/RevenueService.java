package com.apiplatform.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 营收统计服务接口
 */
public interface RevenueService {

    /**
     * 获取总营收概览
     */
    Map<String, Object> getOverview();

    /**
     * 获取今日营收
     */
    Map<String, Object> getTodayRevenue();

    /**
     * 获取本月营收
     */
    Map<String, Object> getMonthRevenue();

    /**
     * 获取营收趋势（按日）
     */
    List<Map<String, Object>> getDailyTrend(LocalDate startDate, LocalDate endDate);

    /**
     * 获取营收趋势（按月）
     */
    List<Map<String, Object>> getMonthlyTrend(int year);

    /**
     * 按支付渠道统计营收
     */
    List<Map<String, Object>> getRevenueByPaymentChannel(LocalDate startDate, LocalDate endDate);

    /**
     * 按业务类型统计营收
     */
    List<Map<String, Object>> getRevenueByBusinessType(LocalDate startDate, LocalDate endDate);

    /**
     * 获取订单统计
     */
    Map<String, Object> getOrderStats(LocalDate startDate, LocalDate endDate);

    /**
     * 获取付费用户统计
     */
    Map<String, Object> getPayingUserStats();

    /**
     * 获取Top消费用户
     */
    List<Map<String, Object>> getTopConsumers(int limit);

    /**
     * 获取代理分成统计
     */
    Map<String, Object> getAgentCommissionStats();

    /**
     * 获取年度营收报表
     */
    Map<String, Object> getYearlyReport(int year);
}
