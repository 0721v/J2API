package com.apiplatform.controller;

import com.apiplatform.common.PageResult;
import com.apiplatform.common.Result;
import com.apiplatform.entity.ConsumptionRecord;
import com.apiplatform.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 报表控制器
 */
@Slf4j
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 获取消费记录列表
     */
    @GetMapping("/consumption")
    public Result<PageResult<ConsumptionRecord>> getConsumptionRecords(
            @RequestAttribute(value = "userId") Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success(reportService.getConsumptionRecords(userId, startDate, endDate, type, page, pageSize));
    }

    /**
     * 获取消费汇总统计
     */
    @GetMapping("/consumption/summary")
    public Result<Map<String, BigDecimal>> getConsumptionSummary(
            @RequestAttribute(value = "userId") Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success(reportService.getConsumptionSummary(userId, startDate, endDate));
    }

    /**
     * 按类型分组统�?     */
    @GetMapping("/consumption/by-type")
    public Result<List<Map<String, Object>>> getConsumptionByType(
            @RequestAttribute(value = "userId") Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success(reportService.getConsumptionByType(userId, startDate, endDate));
    }

    /**
     * 获取消费趋势
     */
    @GetMapping("/consumption/trend")
    public Result<List<Map<String, Object>>> getConsumptionTrend(
            @RequestAttribute(value = "userId") Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success(reportService.getConsumptionTrend(userId, startDate, endDate));
    }

    /**
     * 获取每日统计
     */
    @GetMapping("/daily")
    public Result<PageResult<Map<String, Object>>> getDailyStats(
            @RequestAttribute(value = "userId") Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int pageSize) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success(reportService.getDailyStats(userId, startDate, endDate, page, pageSize));
    }

    /**
     * 获取整体统计
     */
    @GetMapping("/overall")
    public Result<Map<String, Object>> getOverallStats(
            @RequestAttribute(value = "userId") Long userId) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success(reportService.getOverallStats(userId));
    }

    /**
     * 获取本月统计
     */
    @GetMapping("/month")
    public Result<Map<String, Object>> getMonthStats(
            @RequestAttribute(value = "userId") Long userId) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success(reportService.getMonthStats(userId));
    }

    /**
     * 获取今日统计
     */
    @GetMapping("/today")
    public Result<Map<String, Object>> getTodayStats(
            @RequestAttribute(value = "userId") Long userId) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success(reportService.getTodayStats(userId));
    }
}
