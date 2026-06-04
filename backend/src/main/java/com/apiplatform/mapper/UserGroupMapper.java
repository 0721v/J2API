package com.apiplatform.mapper;

import com.apiplatform.entity.UserGroup;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户分组Mapper
 *
 * @author API Platform Team
 */
@Mapper
public interface UserGroupMapper extends BaseMapper<UserGroup> {

    /**
     * 分页查询分组
     */
    IPage<UserGroup> selectGroupPage(Page<UserGroup> page, @Param("keyword") String keyword);

    /**
     * 根据名称查询
     */
    @Select("SELECT * FROM user_groups WHERE name = #{name} AND deleted = false")
    UserGroup selectByName(@Param("name") String name);

    /**
     * 查询所有启用的分组
     */
    @Select("SELECT * FROM user_groups WHERE status = 'enabled' AND deleted = false ORDER BY priority DESC")
    List<UserGroup> selectEnabledGroups();

    /**
     * 根据等级查询分组
     */
    @Select("SELECT * FROM user_groups WHERE level = #{level} AND deleted = false")
    List<UserGroup> selectByLevel(@Param("level") String level);

    /**
     * 查询优先级最高的分组
     */
    @Select("SELECT * FROM user_groups WHERE status = 'enabled' AND deleted = false ORDER BY priority DESC LIMIT 1")
    UserGroup selectTopPriority();
}