package com.apiplatform.mapper;

import com.apiplatform.entity.InviteRewardRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 邀请奖励记录 Mapper
 */
@Mapper
public interface InviteRewardRecordMapper extends BaseMapper<InviteRewardRecord> {

    /**
     * 统计邀请人已发放奖励次数
     */
    int countByInviterId(@Param("inviterId") Long inviterId);

    /**
     * 统计邀请人今日奖励次数
     */
    int countByInviterIdToday(@Param("inviterId") Long inviterId);

    /**
     * 统计被邀请人奖励次数
     */
    int countByInviteeIdAndType(@Param("inviteeId") Long inviteeId, @Param("rewardType") String rewardType);
}
