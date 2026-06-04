package com.apiplatform.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.apiplatform.common.BizException;
import com.apiplatform.common.PageResult;
import com.apiplatform.entity.Channel;
import com.apiplatform.entity.Model;
import com.apiplatform.entity.User;
import com.apiplatform.entity.UserGroup;
import com.apiplatform.mapper.ChannelMapper;
import com.apiplatform.mapper.ModelMapper;
import com.apiplatform.mapper.UserGroupMapper;
import com.apiplatform.mapper.UserMapper;
import com.apiplatform.service.UserGroupService;
import com.apiplatform.util.CacheUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户分组服务实现
 *
 * @author API Platform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserGroupServiceImpl extends ServiceImpl<UserGroupMapper, UserGroup> implements UserGroupService {

    private final UserGroupMapper userGroupMapper;
    private final UserMapper userMapper;
    private final ModelMapper modelMapper;
    private final ChannelMapper channelMapper;
    private final CacheUtil cacheUtil;

    @Value("${system.default-minute-limit:60}")
    private int defaultMinuteLimit;

    @Value("${system.default-daily-limit:10000}")
    private int defaultDailyLimit;

    private static final String USER_GROUP_CACHE_PREFIX = "user:group:";
    private static final String USER_QUOTA_CACHE_PREFIX = "user:quota:";

    @Override
    @Transactional
    public UserGroup createGroup(UserGroup group) {
        if (group.getName() == null || group.getName().trim().isEmpty()) {
            throw new BizException("分组名称不能为空");
        }

        // 检查名称是否重复
        long count = count(new LambdaQueryWrapper<UserGroup>()
                .eq(UserGroup::getName, group.getName())
                .eq(UserGroup::getDeleted, false));
        if (count > 0) {
            throw new BizException("分组名称已存在");
        }

        if (group.getGlobalRate() == null) {
            group.setGlobalRate(BigDecimal.ONE);
        }

        save(group);
        log.info("创建用户分组: {}", group.getName());
        return group;
    }

    @Override
    @Transactional
    public UserGroup updateGroup(Long id, UserGroup group) {
        UserGroup existing = getById(id);
        if (existing == null) {
            throw new BizException("分组不存在");
        }

        if (group.getName() != null && !group.getName().equals(existing.getName())) {
            // 检查名称是否重复
            long count = count(new LambdaQueryWrapper<UserGroup>()
                    .eq(UserGroup::getName, group.getName())
                    .ne(UserGroup::getId, id)
                    .eq(UserGroup::getDeleted, false));
            if (count > 0) {
                throw new BizException("分组名称已存在");
            }
        }

        // 更新字段
        if (group.getName() != null) existing.setName(group.getName());
        if (group.getDescription() != null) existing.setDescription(group.getDescription());
        if (group.getPriority() != null) existing.setPriority(group.getPriority());
        if (group.getStatus() != null) existing.setStatus(group.getStatus());
        if (group.getDefaultMinuteLimit() != null) existing.setDefaultMinuteLimit(group.getDefaultMinuteLimit());
        if (group.getDefaultDailyLimit() != null) existing.setDefaultDailyLimit(group.getDefaultDailyLimit());
        if (group.getDefaultMonthlyLimit() != null) existing.setDefaultMonthlyLimit(group.getDefaultMonthlyLimit());
        if (group.getGlobalRate() != null) existing.setGlobalRate(group.getGlobalRate());
        if (group.getModelRates() != null) existing.setModelRates(group.getModelRates());
        if (group.getChannelRates() != null) existing.setChannelRates(group.getChannelRates());
        if (group.getAllowedModels() != null) existing.setAllowedModels(group.getAllowedModels());
        if (group.getBlockedModels() != null) existing.setBlockedModels(group.getBlockedModels());
        if (group.getAllowedChannels() != null) existing.setAllowedChannels(group.getAllowedChannels());
        if (group.getAllowRecharge() != null) existing.setAllowRecharge(group.getAllowRecharge());
        if (group.getAllowPackages() != null) existing.setAllowPackages(group.getAllowPackages());
        if (group.getAllowUsageStats() != null) existing.setAllowUsageStats(group.getAllowUsageStats());
        if (group.getLevel() != null) existing.setLevel(group.getLevel());
        if (group.getValidDays() != null) existing.setValidDays(group.getValidDays());
        if (group.getRemark() != null) existing.setRemark(group.getRemark());

        updateById(existing);
        clearUserCache();
        log.info("更新用户分组: {}", existing.getName());
        return existing;
    }

    @Override
    @Transactional
    public void deleteGroup(Long id) {
        UserGroup group = getById(id);
        if (group == null) {
            throw new BizException("分组不存在");
        }

        // 检查分组是否有用户
        long userCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getGroupId, id)
                .eq(User::getDeleted, false));
        if (userCount > 0) {
            throw new BizException("分组下仍有用户，无法删除");
        }

        removeById(id);
        clearUserCache();
        log.info("删除用户分组: {}", group.getName());
    }

    @Override
    public UserGroup getGroupById(Long id) {
        return getById(id);
    }

    @Override
    public List<UserGroup> getAllGroups() {
        return list(new LambdaQueryWrapper<UserGroup>()
                .eq(UserGroup::getDeleted, false)
                .orderByDesc(UserGroup::getPriority)
                .orderByAsc(UserGroup::getCreatedAt));
    }

    @Override
    public PageResult<UserGroup> getGroupPage(int page, int size, String keyword) {
        LambdaQueryWrapper<UserGroup> wrapper = new LambdaQueryWrapper<UserGroup>()
                .eq(UserGroup::getDeleted, false)
                .orderByDesc(UserGroup::getPriority);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(UserGroup::getName, keyword)
                    .or()
                    .like(UserGroup::getDescription, keyword));
        }

        Page<UserGroup> result = page(new Page<>(page, size), wrapper);

        // 填充用户数量
        result.getRecords().forEach(group -> {
            long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getGroupId, group.getId())
                    .eq(User::getDeleted, false));
            group.setUserCount((int) count);
        });

        return PageResult.of(result.getRecords(), result.getTotal(), (long) page, (long) size);
    }

    @Override
    @Transactional
    public void assignUserToGroup(Long userId, Long groupId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        UserGroup group = getById(groupId);
        if (group == null) {
            throw new BizException("分组不存在");
        }

        user.setGroupId(groupId);

        // 如果分组有有效期，设置用户过期时间
        if (group.getValidDays() != null && group.getValidDays() > 0) {
            user.setExpireTime(LocalDateTime.now().plusDays(group.getValidDays()));
        } else {
            user.setExpireTime(null);
        }

        userMapper.updateById(user);
        clearUserCache(userId);
        log.info("用户 {} 分配到分组 {}", user.getUsername(), group.getName());
    }

    @Override
    @Transactional
    public void removeUserFromGroup(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        user.setGroupId(null);
        user.setExpireTime(null);
        userMapper.updateById(user);
        clearUserCache(userId);
        log.info("用户 {} 从分组移除", user.getUsername());
    }

    @Override
    public UserGroup getUserGroup(Long userId) {
        String cacheKey = USER_GROUP_CACHE_PREFIX + userId;
        UserGroup cached = cacheUtil.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        User user = userMapper.selectById(userId);
        if (user == null || user.getGroupId() == null) {
            return null;
        }

        UserGroup group = getById(user.getGroupId());
        if (group != null) {
            cacheUtil.set(cacheKey, group, 30, TimeUnit.MINUTES);
        }

        return group;
    }

    @Override
    public Map<String, Object> getUserQuota(Long userId) {
        String cacheKey = USER_QUOTA_CACHE_PREFIX + userId;
        Map<String, Object> cached = cacheUtil.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Map<String, Object> quota = new HashMap<>();
        UserGroup group = getUserGroup(userId);

        // 分钟限制
        int minuteLimit = defaultMinuteLimit;
        if (group != null && group.getDefaultMinuteLimit() != null && group.getDefaultMinuteLimit() > 0) {
            minuteLimit = group.getDefaultMinuteLimit();
        }
        quota.put("minuteLimit", minuteLimit);

        // 日限制
        int dailyLimit = defaultDailyLimit;
        if (group != null && group.getDefaultDailyLimit() != null && group.getDefaultDailyLimit() > 0) {
            dailyLimit = group.getDefaultDailyLimit();
        }
        quota.put("dailyLimit", dailyLimit);

        // 月限制
        int monthlyLimit = 0;
        if (group != null && group.getDefaultMonthlyLimit() != null) {
            monthlyLimit = group.getDefaultMonthlyLimit();
        }
        quota.put("monthlyLimit", monthlyLimit);

        // 价格倍率
        double globalRate = 1.0;
        if (group != null && group.getGlobalRate() != null) {
            globalRate = group.getGlobalRate().doubleValue();
        }
        quota.put("globalRate", globalRate);

        // 权限
        quota.put("allowRecharge", group == null || group.getAllowRecharge());
        quota.put("allowPackages", group == null || group.getAllowPackages());
        quota.put("allowUsageStats", group == null || group.getAllowUsageStats());

        // 分组信息
        if (group != null) {
            quota.put("groupId", group.getId());
            quota.put("groupName", group.getName());
            quota.put("groupLevel", group.getLevel());
            quota.put("groupLevelName", group.getLevelName());
        }

        cacheUtil.set(cacheKey, quota, 15, TimeUnit.MINUTES);
        return quota;
    }

    @Override
    public boolean isModelAllowed(Long userId, String modelName) {
        UserGroup group = getUserGroup(userId);
        if (group == null) {
            return true; // 无分组，默认允许
        }
        return group.isModelAllowed(modelName);
    }

    @Override
    public double calculatePrice(Long userId, String modelName, String channelName, double basePrice) {
        BigDecimal price = BigDecimal.valueOf(basePrice);
        UserGroup group = getUserGroup(userId);

        if (group != null) {
            price = group.calculatePrice(price, modelName, channelName);
        }

        return price.doubleValue();
    }

    @Override
    public double getUserRate(Long userId, String modelName, String channelName) {
        UserGroup group = getUserGroup(userId);
        if (group == null) {
            return 1.0;
        }

        BigDecimal modelRate = group.getModelRate(modelName);
        BigDecimal channelRate = group.getChannelRate(channelName);
        BigDecimal globalRate = group.getGlobalRate();

        return modelRate.multiply(channelRate).multiply(globalRate).doubleValue();
    }

    @Override
    public int getUserCountInGroup(Long groupId) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getGroupId, groupId)
                .eq(User::getDeleted, false));
        return count != null ? count.intValue() : 0;
    }

    @Override
    @Transactional
    public UserGroup copyGroup(Long sourceGroupId, String newName) {
        UserGroup source = getById(sourceGroupId);
        if (source == null) {
            throw new BizException("源分组不存在");
        }

        UserGroup copy = UserGroup.builder()
                .name(newName)
                .description("复制自: " + source.getName())
                .priority(source.getPriority())
                .status("disabled") // 复制的分组默认禁用
                .defaultMinuteLimit(source.getDefaultMinuteLimit())
                .defaultDailyLimit(source.getDefaultDailyLimit())
                .defaultMonthlyLimit(source.getDefaultMonthlyLimit())
                .globalRate(source.getGlobalRate())
                .modelRates(source.getModelRates())
                .channelRates(source.getChannelRates())
                .allowedModels(source.getAllowedModels())
                .blockedModels(source.getBlockedModels())
                .allowedChannels(source.getAllowedChannels())
                .allowRecharge(source.getAllowRecharge())
                .allowPackages(source.getAllowPackages())
                .allowUsageStats(source.getAllowUsageStats())
                .level(source.getLevel())
                .validDays(source.getValidDays())
                .remark("复制分组")
                .build();

        save(copy);
        log.info("复制分组: {} -> {}", source.getName(), newName);
        return copy;
    }

    @Override
    @Transactional
    public void updateGroupPriorities(List<Long> groupIds) {
        for (int i = 0; i < groupIds.size(); i++) {
            UserGroup group = new UserGroup();
            group.setId(groupIds.get(i));
            group.setPriority(groupIds.size() - i);
            updateById(group);
        }
        clearUserCache();
    }

    /**
     * 清除用户缓存
     */
    private void clearUserCache(Long userId) {
        cacheUtil.delete(USER_GROUP_CACHE_PREFIX + userId);
        cacheUtil.delete(USER_QUOTA_CACHE_PREFIX + userId);
    }

    /**
     * 清除所有用户缓存
     */
    private void clearUserCache() {
        // 内存缓存模式下，无法通过前缀删除，这里不做操作
        // 缓存会自动过期
        log.debug("缓存清除请求已接收（内存缓存模式下自动过期）");
    }
}
