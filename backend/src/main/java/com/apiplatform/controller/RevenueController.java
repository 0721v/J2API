package com.apiplatform.controller;

import com.apiplatform.common.Result;
import com.apiplatform.service.RevenueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 营收统计控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/revenue")
@RequiredArgsConstructor
public class RevenueController {

    private final RevenueService revenueService;

    /**
     * 获取营收概览
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        return Result.success(revenueService.getOverview());
    }

    /**
     * 获取今日营收
     */
    @GetMapping("/today")
    public Result<Map<String, Object>> getTodayRevenue() {
        return Result.success(revenueService.getTodayRevenue());
    }

    /**
     * 获取本月营收
     */
    @GetMapping("/month")
    public Result<Map<String, Object>> getMonthRevenue() {
        return Result.success(revenueService.getMonthRevenue());
    }

    /**
     * 获取营收趋势（日度）
     */
    @GetMapping("/trend/daily")
    public Result<List<Map<String, Object>>> getDailyTrend(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(revenueService.getDailyTrend(startDate, endDate));
    }

    /**
     * 获取营收趋势（月�?     */
    @GetMapping("/trend/monthly")
    public Result<List<Map<String, Object>>> getMonthlyTrend(@RequestParam int year) {
        return Result.success(revenueService.getMonthlyTrend(year));
    }

    /**
     * 按支付渠道统�?     */
    @GetMapping("/by-channel")
    public Result<List<Map<String, Object>>> getRevenueByChannel(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(revenueService.getRevenueByPaymentChannel(startDate, endDate));
    }

    /**
     * 按业务类型统�?     */
    @GetMapping("/by-business")
    public Result<List<Map<String, Object>>> getRevenueByBusiness(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(revenueService.getRevenueByBusinessType(startDate, endDate));
    }

    /**
     * 获取订单统计
     */
    @GetMapping("/orders")
    public Result<Map<String, Object>> getOrderStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(revenueService.getOrderStats(startDate, endDate));
    }

    /**
     * 获取付费用户统计
     */
    @GetMapping("/users")
    public Result<Map<String, Object>> getPayingUserStats() {
        return Result.success(revenueService.getPayingUserStats());
    }

    /**
     * 获取Top消费用户
     */
    @GetMapping("/top-users")
    public Result<List<Map<String, Object>>> getTopConsumers(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(revenueService.getTopConsumers(limit));
    }

    /**
     * 获取代理分成统计
     */
    @GetMapping("/agent-commission")
    public Result<Map<String, Object>> getAgentCommissionStats() {
        return Result.success(revenueService.getAgentCommissionStats());
    }

    /**
     * 获取年度报表
     */
    @GetMapping("/yearly")
    public Result<Map<String, Object>> getYearlyReport(@RequestParam int year) {
        return Result.success(revenueService.getYearlyReport(year));
    }
}
