package com.apiplatform.mapper;

import com.apiplatform.entity.OAuthProvider;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * OAuth提供商Mapper
 *
 * @author API Platform Team
 */
@Mapper
public interface OAuthProviderMapper extends BaseMapper<OAuthProvider> {

    /**
     * 根据提供商类型查询
     */
    @Select("SELECT * FROM oauth_providers WHERE provider = #{provider} AND enabled = true LIMIT 1")
    OAuthProvider selectByProvider(String provider);

    /**
     * 获取所有启用的提供商
     */
    @Select("SELECT * FROM oauth_providers WHERE enabled = true ORDER BY sort ASC")
    java.util.List<OAuthProvider> selectEnabledProviders();
}
