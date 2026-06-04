package com.apiplatform.mapper;

import com.apiplatform.entity.Channel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 渠道Mapper
 *
 * @author API Platform Team
 */
@Mapper
public interface ChannelMapper extends BaseMapper<Channel> {

    /**
     * 分页查询渠道
     */
    IPage<Channel> selectChannelPage(Page<Channel> page, @Param("keyword") String keyword, 
                                     @Param("type") String type, @Param("status") String status);

    /**
     * 查询可用渠道
     */
    @Select("SELECT * FROM channels WHERE status = 'active' AND deleted = false ORDER BY priority DESC, weight DESC")
    List<Channel> selectAvailable();

    /**
     * 查询指定类型的可用渠道
     */
    @Select("SELECT * FROM channels WHERE type = #{type} AND status = 'active' AND deleted = false ORDER BY priority DESC")
    List<Channel> selectAvailableByType(@Param("type") String type);

    /**
     * 根据权重随机选择渠道
     */
    @Select("SELECT * FROM channels WHERE status = 'active' AND deleted = false ORDER BY RAND() * weight DESC LIMIT 1")
    Channel selectRandomByWeight();

    /**
     * 查询备用渠道
     */
    @Select("SELECT * FROM channels WHERE id = #{channelId} AND deleted = false")
    Channel selectById(@Param("channelId") Long channelId);

    /**
     * 统计各类型渠道数量
     */
    @Select("SELECT type, COUNT(*) as count FROM channels WHERE deleted = false GROUP BY type")
    List<java.util.Map<String, Object>> selectCountByType();

    /**
     * 查询故障渠道
     */
    @Select("SELECT * FROM channels WHERE failure_count >= max_retries AND deleted = false")
    List<Channel> selectFailedChannels();
}
