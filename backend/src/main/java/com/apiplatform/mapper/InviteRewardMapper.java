package com.apiplatform.mapper;

import com.apiplatform.entity.InviteReward;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 邀请奖励配置 Mapper
 */
@Mapper
public interface InviteRewardMapper extends BaseMapper<InviteReward> {

    /**
     * 获取有效奖励配置
     */
    @Select("SELECT * FROM invite_rewards WHERE status = 'active' " +
            "AND (start_time IS NULL OR start_time <= NOW()) " +
            "AND (end_time IS NULL OR end_time >= NOW()) " +
            "ORDER BY priority DESC")
    List<InviteReward> selectActiveRewards();

    /**
     * 根据类型获取有效奖励配置
     */
    @Select("SELECT * FROM invite_rewards WHERE status = 'active' AND type = #{type} " +
            "AND (start_time IS NULL OR start_time <= NOW()) " +
            "AND (end_time IS NULL OR end_time >= NOW()) " +
            "ORDER BY priority DESC")
    List<InviteReward> selectActiveRewardsByType(@Param("type") String type);
}
