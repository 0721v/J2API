package com.apiplatform.mapper;

import com.apiplatform.entity.OAuthBinding;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * OAuth绑定Mapper
 *
 * @author API Platform Team
 */
@Mapper
public interface OAuthBindingMapper extends BaseMapper<OAuthBinding> {

    /**
     * 根据提供商和用户ID查询绑定
     */
    @Select("SELECT * FROM oauth_bindings WHERE provider = #{provider} AND provider_user_id = #{providerUserId} AND deleted = false LIMIT 1")
    OAuthBinding selectByProviderAndUserId(String provider, String providerUserId);

    /**
     * 根据用户ID和提供商查询绑定
     */
    @Select("SELECT * FROM oauth_bindings WHERE user_id = #{userId} AND provider = #{provider} AND deleted = false LIMIT 1")
    OAuthBinding selectByUserAndProvider(Long userId, String provider);

    /**
     * 获取用户的所有绑定
     */
    @Select("SELECT * FROM oauth_bindings WHERE user_id = #{userId} AND deleted = false ORDER BY bound_at DESC")
    java.util.List<OAuthBinding> selectByUserId(Long userId);

    /**
     * 删除用户的指定绑定
     */
    @Delete("DELETE FROM oauth_bindings WHERE user_id = #{userId} AND provider = #{provider}")
    int deleteByUserAndProvider(Long userId, String provider);
}
