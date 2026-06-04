package com.apiplatform.service.impl;

import com.apiplatform.mapper.RevenueMapper;
import com.apiplatform.service.RevenueService;
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
 * 营收统计服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RevenueServiceImpl implements RevenueService {

    private final RevenueMapper revenueMapper;

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new HashMap<>();

        // 营收数据
        BigDecimal totalRevenue = revenueMapper.getTotalRevenue();
        Long totalOrders = revenueMapper.getTotalOrderCount();
        Long payingUsers = revenueMapper.getPayingUserCount();

        // API消费
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfToday = LocalDate.now();
        Map<String, Object> apiConsumption = revenueMapper.getApiConsumption(startOfMonth, endOfToday);

        // 代理分成
        Map<String, Object> agentCommission = revenueMapper.getTotalAgentCommission();
        BigDecimal pendingCommission = revenueMapper.getPendingAgentCommission();

        overview.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        overview.put("totalOrders", totalOrders != null ? totalOrders : 0);
        overview.put("payingUsers", payingUsers != null ? payingUsers : 0);
        overview.put("apiConsumption", apiConsumption.getOrDefault("total_cost", BigDecimal.ZERO));
        overview.put("agentCommission", agentCommission.getOrDefault("total_commission", BigDecimal.ZERO));
        overview.put("pendingCommission", pendingCommission != null ? pendingCommission : BigDecimal.ZERO);
        overview.put("netRevenue", calculateNetRevenue(totalRevenue, agentCommission));

        return overview;
    }

    @Override
    public Map<String, Object> getTodayRevenue() {
        Map<String, Object> revenue = new HashMap<>();

        BigDecimal todayRevenue = revenueMapper.getTodayRevenue();
        Long todayOrders = revenueMapper.getTodayOrderCount();
        Long todayNewUsers = revenueMapper.getTodayNewPayingUsers();

        LocalDate startOfDay = LocalDate.now();
        LocalDate endOfDay = LocalDate.now();
        Map<String, Object> apiConsumption = revenueMapper.getApiConsumption(startOfDay, endOfDay);

        revenue.put("revenue", todayRevenue != null ? todayRevenue : BigDecimal.ZERO);
        revenue.put("orders", todayOrders != null ? todayOrders : 0);
        revenue.put("newPayingUsers", todayNewUsers != null ? todayNewUsers : 0);
        revenue.put("apiConsumption", apiConsumption.getOrDefault("total_cost", BigDecimal.ZERO));
        revenue.put("apiCalls", apiConsumption.getOrDefault("call_count", 0L));

        return revenue;
    }

    @Override
    public Map<String, Object> getMonthRevenue() {
        Map<String, Object> revenue = new HashMap<>();

        BigDecimal monthRevenue = revenueMapper.getMonthRevenue();
        Long monthOrders = revenueMapper.getTotalOrderCount(); // 简化计算
        Long payingUsers = revenueMapper.getPayingUserCount();

        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfDay = LocalDate.now();
        Map<String, Object> apiConsumption = revenueMapper.getApiConsumption(startOfMonth, endOfDay);

        revenue.put("revenue", monthRevenue != null ? monthRevenue : BigDecimal.ZERO);
        revenue.put("orders", monthOrders != null ? monthOrders : 0);
        revenue.put("payingUsers", payingUsers != null ? payingUsers : 0);
        revenue.put("apiConsumption", apiConsumption.getOrDefault("total_cost", BigDecimal.ZERO));

        // 计算日均
        int dayOfMonth = LocalDate.now().getDayOfMonth();
        if (dayOfMonth > 0 && monthRevenue != null) {
            BigDecimal dailyAvg = monthRevenue.divide(BigDecimal.valueOf(dayOfMonth), 2, java.math.RoundingMode.HALF_UP);
            revenue.put("dailyAvg", dailyAvg);
        }

        return revenue;
    }

    @Override
    public List<Map<String, Object>> getDailyTrend(LocalDate startDate, LocalDate endDate) {
        return revenueMapper.getDailyRevenueTrend(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getMonthlyTrend(int year) {
        return revenueMapper.getMonthlyRevenueTrend(year);
    }

    @Override
    public List<Map<String, Object>> getRevenueByPaymentChannel(LocalDate startDate, LocalDate endDate) {
        return revenueMapper.getRevenueByChannel(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getRevenueByBusinessType(LocalDate startDate, LocalDate endDate) {
        return revenueMapper.getRevenueByBusinessType(startDate, endDate);
    }

    @Override
    public Map<String, Object> getOrderStats(LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> dailyStats = revenueMapper.getDailyRevenueTrend(startDate, endDate);

        long totalOrders = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalUsers = 0;

        for (Map<String, Object> stat : dailyStats) {
            Object orders = stat.get("order_count");
            Object revenue = stat.get("revenue");
            Object users = stat.get("user_count");

            totalOrders += orders != null ? ((Number) orders).longValue() : 0;
            totalRevenue = totalRevenue.add(revenue != null ? (BigDecimal) revenue : BigDecimal.ZERO);
            totalUsers += users != null ? ((Number) users).longValue() : 0;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalOrders", totalOrders);
        result.put("totalRevenue", totalRevenue);
        result.put("totalUsers", totalUsers);
        result.put("avgOrderValue", totalOrders > 0 ?
                totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, java.math.RoundingMode.HALF_UP) :
                BigDecimal.ZERO);

        return result;
    }

    @Override
    public Map<String, Object> getPayingUserStats() {
        Map<String, Object> stats = new HashMap<>();

        Long payingUsers = revenueMapper.getPayingUserCount();
        Long todayNewUsers = revenueMapper.getTodayNewPayingUsers();

        stats.put("totalPayingUsers", payingUsers != null ? payingUsers : 0);
        stats.put("todayNewPayingUsers", todayNewUsers != null ? todayNewUsers : 0);

        return stats;
    }

    @Override
    public List<Map<String, Object>> getTopConsumers(int limit) {
        return revenueMapper.getTopConsumers(limit);
    }

    @Override
    public Map<String, Object> getAgentCommissionStats() {
        Map<String, Object> stats = revenueMapper.getTotalAgentCommission();
        BigDecimal pending = revenueMapper.getPendingAgentCommission();

        stats.put("pendingCommission", pending != null ? pending : BigDecimal.ZERO);

        return stats;
    }

    @Override
    public Map<String, Object> getYearlyReport(int year) {
        Map<String, Object> report = new HashMap<>();

        List<Map<String, Object>> monthlyTrend = getMonthlyTrend(year);

        long totalOrders = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (Map<String, Object> month : monthlyTrend) {
            Object orders = month.get("order_count");
            Object revenue = month.get("revenue");

            totalOrders += orders != null ? ((Number) orders).longValue() : 0;
            totalRevenue = totalRevenue.add(revenue != null ? (BigDecimal) revenue : BigDecimal.ZERO);
        }

        report.put("year", year);
        report.put("totalOrders", totalOrders);
        report.put("totalRevenue", totalRevenue);
        report.put("monthlyData", monthlyTrend);

        return report;
    }

    /**
     * 计算净营收（扣除代理分成）
     */
    private BigDecimal calculateNetRevenue(BigDecimal totalRevenue, Map<String, Object> agentCommission) {
        BigDecimal commission = (BigDecimal) agentCommission.getOrDefault("total_commission", BigDecimal.ZERO);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        if (commission == null) commission = BigDecimal.ZERO;
        return totalRevenue.subtract(commission);
    }
}
