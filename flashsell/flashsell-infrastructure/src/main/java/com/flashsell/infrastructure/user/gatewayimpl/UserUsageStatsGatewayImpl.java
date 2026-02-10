package com.flashsell.infrastructure.user.gatewayimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flashsell.domain.user.entity.UserUsageStats;
import com.flashsell.domain.user.gateway.UserUsageStatsGateway;
import com.flashsell.infrastructure.user.convertor.UserUsageStatsConvertor;
import com.flashsell.infrastructure.user.dataobject.UserUsageStatsDO;
import com.flashsell.infrastructure.user.mapper.UserUsageStatsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户使用统计网关实现
 */
@Repository
@RequiredArgsConstructor
public class UserUsageStatsGatewayImpl implements UserUsageStatsGateway {
    
    private final UserUsageStatsMapper userUsageStatsMapper;
    private final UserUsageStatsConvertor userUsageStatsConvertor;
    
    @Override
    public Optional<UserUsageStats> findByUserIdAndMonth(Long userId, String month) {
        LambdaQueryWrapper<UserUsageStatsDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserUsageStatsDO::getUserId, userId)
               .eq(UserUsageStatsDO::getMonth, month);
        
        UserUsageStatsDO statsDO = userUsageStatsMapper.selectOne(wrapper);
        return Optional.ofNullable(userUsageStatsConvertor.toEntity(statsDO));
    }
    
    @Override
    public UserUsageStats save(UserUsageStats stats) {
        UserUsageStatsDO statsDO = userUsageStatsConvertor.toDO(stats);
        userUsageStatsMapper.insert(statsDO);
        stats.setId(statsDO.getId());
        return stats;
    }
    
    @Override
    public void update(UserUsageStats stats) {
        UserUsageStatsDO statsDO = userUsageStatsConvertor.toDO(stats);
        userUsageStatsMapper.updateById(statsDO);
    }
    
    @Override
    public UserUsageStats getOrCreate(Long userId, String month) {
        Optional<UserUsageStats> statsOpt = findByUserIdAndMonth(userId, month);
        
        if (statsOpt.isPresent()) {
            return statsOpt.get();
        }
        
        // 创建新的统计记录
        UserUsageStats stats = UserUsageStats.builder()
                .userId(userId)
                .month(month)
                .searchCount(0)
                .exportCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        return save(stats);
    }
}
