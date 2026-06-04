package com.apiplatform.mapper;

import com.apiplatform.entity.TokenGroup;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 令牌分组Mapper
 *
 * @author API Platform Team
 */
@Mapper
public interface TokenGroupMapper extends BaseMapper<TokenGroup> {

    /**
     * 根据用户ID查询分组
     */
    @Select("SELECT * FROM token_groups WHERE user_id = #{userId} AND status = 'active' AND deleted = false ORDER BY priority DESC")
    List<TokenGroup> selectByUserId(@Param("userId") Long userId);
}
