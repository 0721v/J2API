package com.apiplatform.mapper;

import com.apiplatform.entity.Package;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 套餐Mapper
 *
 * @author API Platform Team
 */
@Mapper
public interface PackageMapper extends BaseMapper<Package> {

    /**
     * 分页查询套餐
     */
    IPage<Package> selectPackagePage(Page<Package> page, @Param("keyword") String keyword, 
                                     @Param("type") String type, @Param("visible") Boolean visible);

    /**
     * 查询可见套餐
     */
    @Select("SELECT * FROM packages WHERE visible = true AND deleted = false ORDER BY sort_order, price")
    List<Package> selectVisible();

    /**
     * 查询指定类型的可见套餐
     */
    @Select("SELECT * FROM packages WHERE type = #{type} AND visible = true AND deleted = false ORDER BY sort_order, price")
    List<Package> selectVisibleByType(@Param("type") String type);

    /**
     * 查询推荐套餐
     */
    @Select("SELECT * FROM packages WHERE recommended = true AND visible = true AND deleted = false ORDER BY sort_order")
    List<Package> selectRecommended();

    /**
     * 查询热门套餐
     */
    @Select("SELECT * FROM packages WHERE hot = true AND visible = true AND deleted = false ORDER BY sort_order")
    List<Package> selectHot();

    /**
     * 查询可用套餐（未达购买上限）
     */
    @Select("SELECT p.* FROM packages p LEFT JOIN (SELECT package_id, COUNT(*) as purchase_count FROM user_packages WHERE user_id = #{userId} AND deleted = false GROUP BY package_id) up ON p.id = up.package_id " +
            "WHERE p.visible = true AND (p.purchase_limit = 0 OR up.purchase_count IS NULL OR up.purchase_count < p.purchase_limit) AND p.deleted = false " +
            "ORDER BY p.sort_order")
    List<Package> selectAvailableForUser(@Param("userId") Long userId);
}
