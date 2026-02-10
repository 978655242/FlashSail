package com.flashsell.domain.history.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 搜索历史领域实体
 * 记录用户的搜索查询和结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistory {
    
    /**
     * 搜索历史ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 搜索查询内容
     */
    private String query;
    
    /**
     * 搜索结果数量
     */
    private Integer resultCount;
    
    /**
     * 搜索时间
     */
    private LocalDateTime createdAt;
    
    // ==================== 领域行为 ====================
    
    /**
     * 检查搜索历史是否过期（超过30天）
     * 
     * @return 是否过期
     */
    public boolean isExpired() {
        if (createdAt == null) {
            return true;
        }
        return createdAt.isBefore(LocalDateTime.now().minusDays(30));
    }
    
    /**
     * 检查是否有搜索结果
     * 
     * @return 是否有结果
     */
    public boolean hasResults() {
        return resultCount != null && resultCount > 0;
    }
}
