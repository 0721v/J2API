package com.apiplatform.mapper;

import com.apiplatform.entity.Model;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 模型Mapper
 *
 * @author API Platform Team
 */
@Mapper
public interface ModelMapper extends BaseMapper<Model> {

    /**
     * 分页查询模型
     */
    IPage<Model> selectModelPage(Page<Model> page, @Param("keyword") String keyword, 
                                 @Param("type") String type, @Param("enabled") Boolean enabled);

    /**
     * 根据模型标识查询
     */
    @Select("SELECT * FROM models WHERE model_id = #{modelId} AND deleted = false")
    Model selectByModelId(@Param("modelId") String modelId);

    /**
     * 查询启用的模型
     */
    @Select("SELECT * FROM models WHERE enabled = true AND deleted = false ORDER BY type, name")
    List<Model> selectEnabled();

    /**
     * 查询指定类型的模型
     */
    @Select("SELECT * FROM models WHERE type = #{type} AND enabled = true AND deleted = false ORDER BY name")
    List<Model> selectByType(@Param("type") String type);

    /**
     * 查询指定渠道类型的模型
     */
    @Select("SELECT * FROM models WHERE channel_type = #{channelType} AND enabled = true AND deleted = false")
    List<Model> selectByChannelType(@Param("channelType") String channelType);

    /**
     * 统计各类型模型数量
     */
    @Select("SELECT type, COUNT(*) as count FROM models WHERE deleted = false GROUP BY type")
    List<java.util.Map<String, Object>> selectCountByType();

    /**
     * 查询支持特定功能的模型
     */
    @Select("SELECT * FROM models WHERE enabled = true AND deleted = false" +
            " AND (#{supportsStreaming} IS NULL OR supports_streaming = #{supportsStreaming})" +
            " AND (#{supportsFunctionCall} IS NULL OR supports_function_call = #{supportsFunctionCall})" +
            " AND (#{supportsVision} IS NULL OR supports_vision = #{supportsVision})")
    List<Model> selectByCapabilities(@Param("supportsStreaming") Boolean supportsStreaming,
                                     @Param("supportsFunctionCall") Boolean supportsFunctionCall,
                                     @Param("supportsVision") Boolean supportsVision);
}
