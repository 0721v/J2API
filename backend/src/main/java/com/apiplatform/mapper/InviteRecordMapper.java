package com.apiplatform.mapper;

import com.apiplatform.entity.InviteRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 邀请记录 Mapper
 */
@Mapper
public interface InviteRecordMapper extends BaseMapper<InviteRecord> {

    /**
     * 根据被邀请人ID查询
     */
    InviteRecord selectByInviteeId(@Param("inviteeId") Long inviteeId);

    /**
     * 统计邀请人数
     */
    int countByInviterId(@Param("inviterId") Long inviterId);
}
