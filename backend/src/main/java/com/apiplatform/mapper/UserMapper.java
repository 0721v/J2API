package com.apiplatform.mapper;

import com.apiplatform.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户Mapper
 *
 * @author API Platform Team
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 分页查询用户
     */
    IPage<User> selectUserPage(Page<User> page, @Param("keyword") String keyword);

    /**
     * 根据邮箱查询
     */
    @Select("SELECT * FROM users WHERE email = #{email}")
    User selectByEmail(@Param("email") String email);

    /**
     * 根据用户名查询
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);

    /**
     * 统计用户总数
     */
    @Select("SELECT COUNT(*) FROM users")
    Long selectTotalCount();

    /**
     * 统计新用户数（指定时间内）
     */
    @Select("SELECT COUNT(*) FROM users WHERE created_at >= #{startTime}")
    Long selectNewUserCount(@Param("startTime") LocalDateTime startTime);

    /**
     * 查询用户列表（指定角色）
     */
    @Select("SELECT * FROM users WHERE role = #{role} ORDER BY created_at DESC")
    List<User> selectByRole(@Param("role") String role);

    /**
     * 查询余额Top用户
     */
    @Select("SELECT * FROM users ORDER BY balance DESC LIMIT #{limit}")
    List<User> selectTopBalance(@Param("limit") Integer limit);

    /**
     * 更新最后登录时间和IP
     */
    @Update("UPDATE users SET last_login_at = #{lastLoginAt}, last_login_ip = #{lastLoginIp} WHERE id = #{userId}")
    int updateLastLogin(@Param("userId") Long userId, @Param("lastLoginAt") LocalDateTime lastLoginAt, @Param("lastLoginIp") String lastLoginIp);
}
