package com.apiplatform.mapper;

import com.apiplatform.entity.Token;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 令牌Mapper
 *
 * @author API Platform Team
 */
@Mapper
public interface TokenMapper extends BaseMapper<Token> {

    /**
     * 分页查询用户令牌
     */
    IPage<Token> selectByUserId(Page<Token> page, @Param("userId") Long userId);

    /**
     * 根据API Key查询
     */
    @Select("SELECT * FROM tokens WHERE api_key = #{apiKey} AND deleted = false")
    Token selectByApiKey(@Param("apiKey") String apiKey);

    /**
     * 查询用户所有有效令牌
     */
    @Select("SELECT * FROM tokens WHERE user_id = #{userId} AND status = 'active' AND deleted = false")
    List<Token> selectValidByUserId(@Param("userId") Long userId);

    /**
     * 统计用户令牌数量
     */
    @Select("SELECT COUNT(*) FROM tokens WHERE user_id = #{userId} AND deleted = false")
    Integer countByUserId(@Param("userId") Long userId);

    /**
     * 查询分组下的令牌
     */
    @Select("SELECT * FROM tokens WHERE group_id = #{groupId} AND deleted = false")
    List<Token> selectByGroupId(@Param("groupId") Long groupId);

    /**
     * 查询即将过期的令牌
     */
    @Select("SELECT * FROM tokens WHERE user_id = #{userId} AND expires_at IS NOT NULL " +
            "AND expires_at BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL #{days} DAY) " +
            "AND deleted = false")
    List<Token> selectExpiringSoon(@Param("userId") Long userId, @Param("days") Integer days);
}
