package com.apiplatform.mapper;

import com.apiplatform.entity.UserPackage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户套餐Mapper
 *
 * @author API Platform Team
 */
@Mapper
public interface UserPackageMapper extends BaseMapper<UserPackage> {

    /**
     * 查询用户有效套餐
     */
    @Select("SELECT * FROM user_packages WHERE user_id = #{userId} AND status = 'active' " +
            "AND (expire_time IS NULL OR expire_time > NOW()) AND deleted = false")
    List<UserPackage> selectValidByUserId(@Param("userId") Long userId);

    /**
     * 查询用户套餐（包含过期）
     */
    IPage<UserPackage> selectByUserId(Page<UserPackage> page, @Param("userId") Long userId);

    /**
     * 查询即将过期的套餐
     */
    @Select("SELECT * FROM user_packages WHERE user_id = #{userId} AND status = 'active' " +
            "AND expire_time IS NOT NULL AND expire_time BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL #{days} DAY) " +
            "AND deleted = false")
    List<UserPackage> selectExpiringSoon(@Param("userId") Long userId, @Param("days") Integer days);

    /**
     * 查询用户指定套餐
     */
    @Select("SELECT * FROM user_packages WHERE user_id = #{userId} AND package_id = #{packageId} AND deleted = false")
    List<UserPackage> selectByUserAndPackage(@Param("userId") Long userId, @Param("packageId") Long packageId);

    /**
     * 统计用户有效套餐数量
     */
    @Select("SELECT COUNT(*) FROM user_packages WHERE user_id = #{userId} AND status = 'active' " +
            "AND (expire_time IS NULL OR expire_time > NOW()) AND deleted = false")
    Integer countValidByUserId(@Param("userId") Long userId);

    /**
     * 查询需要自动续期的套餐
     */
    @Select("SELECT * FROM user_packages WHERE auto_renew = true AND status = 'active' " +
            "AND expire_time IS NOT NULL AND expire_time <= DATE_ADD(NOW(), INTERVAL 3 DAY) " +
            "AND deleted = false")
    List<UserPackage> selectNeedAutoRenew();
}
