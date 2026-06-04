package com.apiplatform.controller;

import com.apiplatform.common.PageResult;
import com.apiplatform.common.Result;
import com.apiplatform.entity.ConsumptionRecord;
import com.apiplatform.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "个人报表", description = "个人使用记录和消费记录报表")
public class ReportController {

    private final ReportService reportService;

    /**
     * 获取消费记录列表
     */
    @GetMapping("/consumption")
    @Operation(summary = "获取消费记录", description = "获取指定日期范围的消费记录")
    public Result<PageResult<ConsumptionRecord>> getConsumptionRecords(
            @Parameter(hidden = true) @RequestAttribute(value = "userId") Long userId,
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
    @Operation(summary = "获取消费汇总", description = "获取指定日期范围的消费汇总")
    public Result<Map<String, BigDecimal>> getConsumptionSummary(
            @Parameter(hidden = true) @RequestAttribute(value = "userId") Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success(reportService.getConsumptionSummary(userId, startDate, endDate));
    }

    /**
     * 按类型分组统计
     */
    @GetMapping("/consumption/by-type")
    @Operation(summary = "按类型统计", description = "按消费类型分组统计")
    public Result<List<Map<String, Object>>> getConsumptionByType(
            @Parameter(hidden = true) @RequestAttribute(value = "userId") Long userId,
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
    @Operation(summary = "获取消费趋势", description = "按天统计消费趋势")
    public Result<List<Map<String, Object>>> getConsumptionTrend(
            @Parameter(hidden = true) @RequestAttribute(value = "userId") Long userId,
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
    @Operation(summary = "获取每日统计", description = "获取每日使用和消费统计")
    public Result<PageResult<Map<String, Object>>> getDailyStats(
            @Parameter(hidden = true) @RequestAttribute(value = "userId") Long userId,
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
    @Operation(summary = "获取整体统计", description = "获取累计使用和消费统计")
    public Result<Map<String, Object>> getOverallStats(
            @Parameter(hidden = true) @RequestAttribute(value = "userId") Long userId) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success(reportService.getOverallStats(userId));
    }

    /**
     * 获取本月统计
     */
    @GetMapping("/month")
    @Operation(summary = "获取本月统计", description = "获取当月使用和消费统计")
    public Result<Map<String, Object>> getMonthStats(
            @Parameter(hidden = true) @RequestAttribute(value = "userId") Long userId) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success(reportService.getMonthStats(userId));
    }

    /**
     * 获取今日统计
     */
    @GetMapping("/today")
    @Operation(summary = "获取今日统计", description = "获取今日使用和消费统计")
    public Result<Map<String, Object>> getTodayStats(
            @Parameter(hidden = true) @RequestAttribute(value = "userId") Long userId) {
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success(reportService.getTodayStats(userId));
    }
}
