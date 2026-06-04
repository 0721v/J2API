package com.apiplatform.mapper;

import com.apiplatform.entity.UsageLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 使用日志Mapper
 *
 * @author API Platform Team
 */
@Mapper
public interface UsageLogMapper extends BaseMapper<UsageLog> {

    /**
     * 分页查询用户使用日志
     */
    IPage<UsageLog> selectByUserId(Page<UsageLog> page, @Param("userId") Long userId,
                                   @Param("modelId") Long modelId, @Param("channelId") Long channelId,
                                   @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计用户调用次数
     */
    @Select("SELECT COUNT(*) FROM usage_logs WHERE user_id = #{userId} AND deleted = false")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 统计用户今日调用次数
     */
    @Select("SELECT COUNT(*) FROM usage_logs WHERE user_id = #{userId} " +
            "AND DATE(created_at) = CURDATE() AND deleted = false")
    Long countTodayByUserId(@Param("userId") Long userId);

    /**
     * 统计用户消费总额
     */
    @Select("SELECT COALESCE(SUM(billed_amount), 0) FROM usage_logs WHERE user_id = #{userId} " +
            "AND status = 'success' AND deleted = false")
    Long sumBilledAmountByUserId(@Param("userId") Long userId);

    /**
     * 按模型统计使用量
     */
    @Select("SELECT model_name, COUNT(*) as calls, SUM(billed_amount) as amount " +
            "FROM usage_logs WHERE user_id = #{userId} AND created_at >= #{startTime} AND deleted = false " +
            "GROUP BY model_name ORDER BY amount DESC")
    List<java.util.Map<String, Object>> selectByModel(@Param("userId") Long userId, 
                                                       @Param("startTime") LocalDateTime startTime);

    /**
     * 按渠道统计使用量
     */
    @Select("SELECT channel_name, COUNT(*) as calls, SUM(billed_amount) as amount " +
            "FROM usage_logs WHERE user_id = #{userId} AND created_at >= #{startTime} AND deleted = false " +
            "GROUP BY channel_name ORDER BY amount DESC")
    List<java.util.Map<String, Object>> selectByChannel(@Param("userId") Long userId, 
                                                          @Param("startTime") LocalDateTime startTime);

    /**
     * 查询Token使用统计
     */
    @Select("SELECT DATE(created_at) as date, SUM(request_tokens) as input, SUM(response_tokens) as output " +
            "FROM usage_logs WHERE user_id = #{userId} AND created_at >= #{startTime} AND deleted = false " +
            "GROUP BY DATE(created_at) ORDER BY date")
    List<java.util.Map<String, Object>> selectTokenStats(@Param("userId") Long userId, 
                                                          @Param("startTime") LocalDateTime startTime);

    /**
     * 统计缓存命中率
     */
    @Select("SELECT COUNT(*) as total, SUM(CASE WHEN cache_hit = true THEN 1 ELSE 0 END) as hits " +
            "FROM usage_logs WHERE user_id = #{userId} AND created_at >= #{startTime} AND deleted = false")
    java.util.Map<String, Object> selectCacheStats(@Param("userId") Long userId, 
                                                    @Param("startTime") LocalDateTime startTime);

    /**
     * 查询最近调用
     */
    @Select("SELECT * FROM usage_logs WHERE user_id = #{userId} AND deleted = false " +
            "ORDER BY created_at DESC LIMIT #{limit}")
    List<UsageLog> selectRecentByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);
}
