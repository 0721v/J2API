package com.apiplatform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 营收统计Mapper
 */
@Mapper
public interface RevenueMapper {

    /**
     * 获取总营收金额
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM orders WHERE status = 'completed' AND type IN ('recharge', 'package')")
    BigDecimal getTotalRevenue();

    /**
     * 获取今日营收
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM orders WHERE status = 'completed' AND type IN ('recharge', 'package') AND DATE(created_at) = CURRENT_DATE")
    BigDecimal getTodayRevenue();

    /**
     * 获取本月营收
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM orders WHERE status = 'completed' AND type IN ('recharge', 'package') AND YEAR(created_at) = YEAR(CURRENT_DATE) AND MONTH(created_at) = MONTH(CURRENT_DATE)")
    BigDecimal getMonthRevenue();

    /**
     * 获取总订单数
     */
    @Select("SELECT COUNT(*) FROM orders WHERE status = 'completed'")
    Long getTotalOrderCount();

    /**
     * 获取今日订单数
     */
    @Select("SELECT COUNT(*) FROM orders WHERE status = 'completed' AND DATE(created_at) = CURRENT_DATE")
    Long getTodayOrderCount();

    /**
     * 获取付费用户数
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM orders WHERE status = 'completed' AND type IN ('recharge', 'package')")
    Long getPayingUserCount();

    /**
     * 获取今日新增付费用户数
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM orders WHERE status = 'completed' AND type IN ('recharge', 'package') AND DATE(created_at) = CURRENT_DATE")
    Long getTodayNewPayingUsers();

    /**
     * 按日统计营收
     */
    @Select("SELECT DATE(created_at) as date, " +
            "COUNT(*) as order_count, " +
            "SUM(amount) as revenue, " +
            "COUNT(DISTINCT user_id) as user_count " +
            "FROM orders " +
            "WHERE status = 'completed' AND type IN ('recharge', 'package') " +
            "AND created_at BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY DATE(created_at) ORDER BY date")
    List<Map<String, Object>> getDailyRevenueTrend(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 按月统计营收
     */
    @Select("SELECT YEAR(created_at) as year, MONTH(created_at) as month, " +
            "COUNT(*) as order_count, " +
            "SUM(amount) as revenue, " +
            "COUNT(DISTINCT user_id) as user_count " +
            "FROM orders " +
            "WHERE status = 'completed' AND type IN ('recharge', 'package') " +
            "AND YEAR(created_at) = #{year} " +
            "GROUP BY YEAR(created_at), MONTH(created_at) ORDER BY month")
    List<Map<String, Object>> getMonthlyRevenueTrend(@Param("year") int year);

    /**
     * 按支付渠道统计
     */
    @Select("SELECT payment_channel as channel, " +
            "COUNT(*) as order_count, " +
            "SUM(amount) as revenue " +
            "FROM orders " +
            "WHERE status = 'completed' AND type IN ('recharge', 'package') " +
            "AND created_at BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY payment_channel")
    List<Map<String, Object>> getRevenueByChannel(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 按业务类型统计
     */
    @Select("SELECT type as business_type, " +
            "COUNT(*) as order_count, " +
            "SUM(amount) as revenue " +
            "FROM orders " +
            "WHERE status = 'completed' AND type IN ('recharge', 'package') " +
            "AND created_at BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY type")
    List<Map<String, Object>> getRevenueByBusinessType(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 获取Top消费用户
     */
    @Select("SELECT o.user_id, u.username, u.email, " +
            "COUNT(*) as order_count, " +
            "SUM(o.amount) as total_amount, " +
            "MAX(o.created_at) as last_order_time " +
            "FROM orders o " +
            "LEFT JOIN users u ON o.user_id = u.id " +
            "WHERE o.status = 'completed' AND o.type IN ('recharge', 'package') " +
            "GROUP BY o.user_id, u.username, u.email " +
            "ORDER BY total_amount DESC LIMIT #{limit}")
    List<Map<String, Object>> getTopConsumers(@Param("limit") int limit);

    /**
     * 获取API消费统计
     */
    @Select("SELECT COALESCE(SUM(cost), 0) as total_cost, " +
            "COUNT(*) as call_count " +
            "FROM usage_logs WHERE created_at BETWEEN #{startDate} AND #{endDate}")
    Map<String, Object> getApiConsumption(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 获取代理分成总额
     */
    @Select("SELECT COALESCE(SUM(commission_amount), 0) as total_commission, " +
            "COUNT(*) as settlement_count " +
            "FROM agent_commissions WHERE status = 'settled'")
    Map<String, Object> getTotalAgentCommission();

    /**
     * 获取待结算代理分成
     */
    @Select("SELECT COALESCE(SUM(pending_amount), 0) as pending_commission " +
            "FROM agent_commissions WHERE status = 'pending'")
    BigDecimal getPendingAgentCommission();
}
