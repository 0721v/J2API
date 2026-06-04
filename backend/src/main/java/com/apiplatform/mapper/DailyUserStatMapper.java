package com.apiplatform.mapper;

import com.apiplatform.entity.DailyUserStat;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 每日用户统计Mapper
 */
@Mapper
public interface DailyUserStatMapper extends BaseMapper<DailyUserStat> {

    /**
     * 获取用户在指定日期范围的统计数据
     */
    @Select("SELECT * FROM daily_user_stats WHERE user_id = #{userId} " +
            "AND stat_date BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY stat_date DESC")
    List<DailyUserStat> selectByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 获取用户在指定日期范围的汇总统计
     */
    @Select("SELECT " +
            "SUM(total_api_calls) as total_calls, " +
            "SUM(total_input_tokens) as total_input_tokens, " +
            "SUM(total_output_tokens) as total_output_tokens, " +
            "SUM(total_cost) as total_cost, " +
            "SUM(recharge_amount) as total_recharge, " +
            "SUM(reward_amount) as total_reward " +
            "FROM daily_user_stats WHERE user_id = #{userId} " +
            "AND stat_date BETWEEN #{startDate} AND #{endDate}")
    Map<String, Object> sumByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 按日期分组获取趋势数据
     */
    @Select("SELECT stat_date, total_api_calls, total_cost, recharge_amount " +
            "FROM daily_user_stats WHERE user_id = #{userId} " +
            "AND stat_date BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY stat_date ASC")
    List<Map<String, Object>> selectTrendByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
