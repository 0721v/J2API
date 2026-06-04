package com.apiplatform.service;

import com.apiplatform.entity.UserGroup;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 用户分组服务接口
 *
 * @author API Platform Team
 */
public interface UserGroupService extends IService<UserGroup> {

    /**
     * 创建用户分组
     */
    UserGroup createGroup(UserGroup group);

    /**
     * 更新用户分组
     */
    UserGroup updateGroup(Long id, UserGroup group);

    /**
     * 删除用户分组
     */
    void deleteGroup(Long id);

    /**
     * 获取用户分组详情
     */
    UserGroup getGroupById(Long id);

    /**
     * 获取用户分组列表
     */
    List<UserGroup> getAllGroups();

    /**
     * 分页查询用户分组
     */
    Page<UserGroup> getGroupPage(int page, int size, String keyword);

    /**
     * 将用户分配到分组
     */
    void assignUserToGroup(Long userId, Long groupId);

    /**
     * 将用户从分组移除
     */
    void removeUserFromGroup(Long userId);

    /**
     * 获取用户的分组信息
     */
    UserGroup getUserGroup(Long userId);

    /**
     * 获取用户分组的有效配额
     */
    Map<String, Object> getUserQuota(Long userId);

    /**
     * 检查用户是否有权使用指定模型
     */
    boolean isModelAllowed(Long userId, String modelName);

    /**
     * 计算用户使用指定模型的价格
     */
    double calculatePrice(Long userId, String modelName, String channelName, double basePrice);

    /**
     * 获取用户的价格倍率
     */
    double getUserRate(Long userId, String modelName, String channelName);

    /**
     * 获取分组中的用户数量
     */
    int getUserCountInGroup(Long groupId);

    /**
     * 复制分组配置
     */
    UserGroup copyGroup(Long sourceGroupId, String newName);

    /**
     * 批量更新分组排序
     */
    void updateGroupPriorities(List<Long> groupIds);
}
