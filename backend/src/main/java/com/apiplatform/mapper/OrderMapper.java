package com.apiplatform.mapper;

import com.apiplatform.entity.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单Mapper
 *
 * @author API Platform Team
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 分页查询用户订单
     */
    IPage<Order> selectByUserId(Page<Order> page, @Param("userId") Long userId, @Param("type") String type);

    /**
     * 根据订单号查询
     */
    @Select("SELECT * FROM orders WHERE order_no = #{orderNo} AND deleted = false")
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 查询用户待支付订单
     */
    @Select("SELECT * FROM orders WHERE user_id = #{userId} AND status = 'pending' AND expire_time > NOW() AND deleted = false")
    List<Order> selectPendingByUserId(@Param("userId") Long userId);

    /**
     * 查询过期订单
     */
    @Select("SELECT * FROM orders WHERE status = 'pending' AND expire_time < NOW() AND deleted = false")
    List<Order> selectExpiredPending();

    /**
     * 统计用户订单数
     */
    @Select("SELECT COUNT(*) FROM orders WHERE user_id = #{userId} AND deleted = false")
    Integer countByUserId(@Param("userId") Long userId);

    /**
     * 统计用户消费总额
     */
    @Select("SELECT COALESCE(SUM(paid_amount), 0) FROM orders WHERE user_id = #{userId} AND status = 'paid' AND deleted = false")
    Long sumPaidAmountByUserId(@Param("userId") Long userId);

    /**
     * 按日期统计订单
     */
    @Select("SELECT DATE(created_at) as date, COUNT(*) as count, SUM(paid_amount) as amount " +
            "FROM orders WHERE status = 'paid' AND created_at >= #{startTime} AND deleted = false " +
            "GROUP BY DATE(created_at) ORDER BY date")
    List<java.util.Map<String, Object>> selectDailyStats(@Param("startTime") LocalDateTime startTime);

    /**
     * 查询最新订单
     */
    @Select("SELECT * FROM orders WHERE user_id = #{userId} AND deleted = false ORDER BY created_at DESC LIMIT #{limit}")
    List<Order> selectLatestByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);
}
