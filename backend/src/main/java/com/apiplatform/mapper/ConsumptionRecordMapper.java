package com.apiplatform.mapper;

import com.apiplatform.entity.ConsumptionRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消费记录Mapper
 */
@Mapper
public interface ConsumptionRecordMapper extends BaseMapper<ConsumptionRecord> {

    /**
     * 获取用户在指定时间范围内的消费记录
     */
    @Select("SELECT * FROM consumption_records WHERE user_id = #{userId} " +
            "AND created_at BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<ConsumptionRecord> selectByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("limit") int limit,
            @Param("offset") int offset);

    /**
     * 统计用户在指定时间范围内的消费总额
     */
    @Select("SELECT COALESCE(SUM(CASE WHEN type IN ('recharge', 'reward', 'refund') THEN amount ELSE 0 END), 0) as income, " +
            "COALESCE(SUM(CASE WHEN type = 'api_call' THEN amount ELSE 0 END), 0) as expense " +
            "FROM consumption_records WHERE user_id = #{userId} " +
            "AND created_at BETWEEN #{startTime} AND #{endTime}")
    Map<String, BigDecimal> sumByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 按类型分组统计消费
     */
    @Select("SELECT type, SUM(amount) as total, COUNT(*) as count " +
            "FROM consumption_records WHERE user_id = #{userId} " +
            "AND created_at BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY type")
    List<Map<String, Object>> groupByType(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 按天分组统计消费趋势
     */
    @Select("SELECT DATE(created_at) as date, " +
            "SUM(CASE WHEN type IN ('recharge', 'reward', 'refund') THEN amount ELSE 0 END) as income, " +
            "SUM(CASE WHEN type = 'api_call' THEN amount ELSE 0 END) as expense " +
            "FROM consumption_records WHERE user_id = #{userId} " +
            "AND created_at BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY DATE(created_at) ORDER BY date")
    List<Map<String, Object>> groupByDay(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
