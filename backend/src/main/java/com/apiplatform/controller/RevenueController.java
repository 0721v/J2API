package com.apiplatform.controller;

import com.apiplatform.common.Result;
import com.apiplatform.service.RevenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/admin/revenue")
@RequiredArgsConstructor
@Tag(name = "营收统计", description = "平台营收数据统计接口")
public class RevenueController {

    private final RevenueService revenueService;

    /**
     * 获取营收概览
     */
    @GetMapping("/overview")
    @Operation(summary = "营收概览", description = "获取平台总营收概览数据")
    public Result<Map<String, Object>> getOverview() {
        return Result.success(revenueService.getOverview());
    }

    /**
     * 获取今日营收
     */
    @GetMapping("/today")
    @Operation(summary = "今日营收", description = "获取今日营收数据")
    public Result<Map<String, Object>> getTodayRevenue() {
        return Result.success(revenueService.getTodayRevenue());
    }

    /**
     * 获取本月营收
     */
    @GetMapping("/month")
    @Operation(summary = "本月营收", description = "获取本月营收数据")
    public Result<Map<String, Object>> getMonthRevenue() {
        return Result.success(revenueService.getMonthRevenue());
    }

    /**
     * 获取营收趋势（日）
     */
    @GetMapping("/trend/daily")
    @Operation(summary = "日营收趋势", description = "获取指定日期范围的每日营收趋势")
    public Result<List<Map<String, Object>>> getDailyTrend(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(revenueService.getDailyTrend(startDate, endDate));
    }

    /**
     * 获取营收趋势（月）
     */
    @GetMapping("/trend/monthly")
    @Operation(summary = "月营收趋势", description = "获取指定年份的每月营收趋势")
    public Result<List<Map<String, Object>>> getMonthlyTrend(@RequestParam int year) {
        return Result.success(revenueService.getMonthlyTrend(year));
    }

    /**
     * 按支付渠道统计
     */
    @GetMapping("/by-channel")
    @Operation(summary = "按渠道统计", description = "按支付渠道统计营收")
    public Result<List<Map<String, Object>>> getRevenueByChannel(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(revenueService.getRevenueByPaymentChannel(startDate, endDate));
    }

    /**
     * 按业务类型统计
     */
    @GetMapping("/by-business")
    @Operation(summary = "按业务类型统计", description = "按业务类型（充值/套餐）统计营收")
    public Result<List<Map<String, Object>>> getRevenueByBusiness(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(revenueService.getRevenueByBusinessType(startDate, endDate));
    }

    /**
     * 获取订单统计
     */
    @GetMapping("/orders")
    @Operation(summary = "订单统计", description = "获取指定日期范围的订单统计")
    public Result<Map<String, Object>> getOrderStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(revenueService.getOrderStats(startDate, endDate));
    }

    /**
     * 获取付费用户统计
     */
    @GetMapping("/users")
    @Operation(summary = "付费用户统计", description = "获取付费用户相关统计")
    public Result<Map<String, Object>> getPayingUserStats() {
        return Result.success(revenueService.getPayingUserStats());
    }

    /**
     * 获取Top消费用户
     */
    @GetMapping("/top-users")
    @Operation(summary = "Top用户", description = "获取消费金额最高的用户列表")
    public Result<List<Map<String, Object>>> getTopConsumers(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(revenueService.getTopConsumers(limit));
    }

    /**
     * 获取代理分成统计
     */
    @GetMapping("/agent-commission")
    @Operation(summary = "代理分成统计", description = "获取代理分成相关统计")
    public Result<Map<String, Object>> getAgentCommissionStats() {
        return Result.success(revenueService.getAgentCommissionStats());
    }

    /**
     * 获取年度报表
     */
    @GetMapping("/yearly")
    @Operation(summary = "年度报表", description = "获取指定年份的完整营收报表")
    public Result<Map<String, Object>> getYearlyReport(@RequestParam int year) {
        return Result.success(revenueService.getYearlyReport(year));
    }
}
