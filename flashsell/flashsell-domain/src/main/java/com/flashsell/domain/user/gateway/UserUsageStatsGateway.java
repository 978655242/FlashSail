package com.flashsell.domain.user.gateway;

import com.flashsell.domain.user.entity.UserUsageStats;

import java.util.Optional;

/**
 * 用户使用统计网关接口
 */
public interface UserUsageStatsGateway {
    
    /**
     * 根据用户ID和月份查询使用统计
     * 
     * @param userId 用户ID
     * @param month 月份（格式：YYYY-MM）
     * @return 使用统计（可能为空）
     */
    Optional<UserUsageStats> findByUserIdAndMonth(Long userId, String month);
    
    /**
     * 保存使用统计
     * 
     * @param stats 使用统计
     * @return 保存后的使用统计
     */
    UserUsageStats save(UserUsageStats stats);
    
    /**
     * 更新使用统计
     * 
     * @param stats 使用统计
     */
    void update(UserUsageStats stats);
    
    /**
     * 获取或创建当月使用统计
     * 
     * @param userId 用户ID
     * @param month 月份（格式：YYYY-MM）
     * @return 使用统计
     */
    UserUsageStats getOrCreate(Long userId, String month);
}
