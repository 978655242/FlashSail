package com.flashsell.infrastructure.user.convertor;

import com.flashsell.domain.user.entity.UserUsageStats;
import com.flashsell.infrastructure.user.dataobject.UserUsageStatsDO;
import org.springframework.stereotype.Component;

/**
 * 用户使用统计转换器
 */
@Component
public class UserUsageStatsConvertor {
    
    /**
     * DO转Entity
     * 
     * @param statsDO 数据对象
     * @return 领域实体
     */
    public UserUsageStats toEntity(UserUsageStatsDO statsDO) {
        if (statsDO == null) {
            return null;
        }
        
        return UserUsageStats.builder()
                .id(statsDO.getId())
                .userId(statsDO.getUserId())
                .month(statsDO.getMonth())
                .searchCount(statsDO.getSearchCount())
                .exportCount(statsDO.getExportCount())
                .createdAt(statsDO.getCreatedAt())
                .updatedAt(statsDO.getUpdatedAt())
                .build();
    }
    
    /**
     * Entity转DO
     * 
     * @param stats 领域实体
     * @return 数据对象
     */
    public UserUsageStatsDO toDO(UserUsageStats stats) {
        if (stats == null) {
            return null;
        }
        
        return UserUsageStatsDO.builder()
                .id(stats.getId())
                .userId(stats.getUserId())
                .month(stats.getMonth())
                .searchCount(stats.getSearchCount())
                .exportCount(stats.getExportCount())
                .createdAt(stats.getCreatedAt())
                .updatedAt(stats.getUpdatedAt())
                .build();
    }
}
