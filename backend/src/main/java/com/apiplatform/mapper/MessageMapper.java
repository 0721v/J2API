package com.apiplatform.mapper;

import com.apiplatform.entity.Message;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 站内消息Mapper
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    /**
     * 获取用户未读消息数量
     */
    @Select("SELECT COUNT(*) FROM messages WHERE user_id = #{userId} AND is_read = 0 AND is_deleted = 0 " +
            "AND (expires_at IS NULL OR expires_at > NOW())")
    int countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 获取用户消息列表（分页）
     */
    @Select("SELECT * FROM messages WHERE user_id = #{userId} AND is_deleted = 0 " +
            "AND (expires_at IS NULL OR expires_at > NOW()) " +
            "ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<Message> selectByUserId(@Param("userId") Long userId, @Param("limit") int limit, @Param("offset") int offset);

    /**
     * 标记消息为已读
     */
    @Update("UPDATE messages SET is_read = 1, read_at = NOW() WHERE id = #{messageId} AND user_id = #{userId}")
    int markAsRead(@Param("messageId") Long messageId, @Param("userId") Long userId);

    /**
     * 标记所有消息为已读
     */
    @Update("UPDATE messages SET is_read = 1, read_at = NOW() WHERE user_id = #{userId} AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId);
}
