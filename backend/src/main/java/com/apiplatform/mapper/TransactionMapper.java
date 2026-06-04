package com.apiplatform.mapper;

import com.apiplatform.entity.Transaction;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易记录Mapper
 *
 * @author API Platform Team
 */
@Mapper
public interface TransactionMapper extends BaseMapper<Transaction> {

    /**
     * 分页查询用户交易记录
     */
    IPage<Transaction> selectByUserId(Page<Transaction> page, @Param("userId") Long userId, 
                                       @Param("type") String type, @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 根据交易号查询
     */
    @Select("SELECT * FROM transactions WHERE transaction_no = #{transactionNo} AND deleted = false")
    Transaction selectByTransactionNo(@Param("transactionNo") String transactionNo);

    /**
     * 统计用户收支
     */
    @Select("SELECT type, SUM(amount) as total FROM transactions WHERE user_id = #{userId} " +
            "AND status = 'completed' AND deleted = false GROUP BY type")
    List<java.util.Map<String, Object>> sumByType(@Param("userId") Long userId);

    /**
     * 查询用户余额变动记录
     */
    @Select("SELECT * FROM transactions WHERE user_id = #{userId} AND type IN ('recharge', 'refund', 'adjust') " +
            "AND status = 'completed' AND deleted = false ORDER BY created_at DESC LIMIT #{limit}")
    List<Transaction> selectRecentBalanceChanges(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 统计日消费
     */
    @Select("SELECT DATE(created_at) as date, SUM(ABS(amount)) as amount " +
            "FROM transactions WHERE user_id = #{userId} AND amount < 0 AND status = 'completed' " +
            "AND created_at >= #{startTime} AND deleted = false " +
            "GROUP BY DATE(created_at) ORDER BY date")
    List<java.util.Map<String, Object>> selectDailyConsumption(@Param("userId") Long userId, 
                                                                 @Param("startTime") LocalDateTime startTime);
}
