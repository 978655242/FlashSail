package com.flashsell.domain.history.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 浏览历史领域实体
 * 记录用户浏览的产品
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrowseHistory {
    
    /**
     * 浏览历史ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 产品ID
     */
    private Long productId;
    
    /**
     * 浏览时间（重复浏览更新此时间）
     */
    private LocalDateTime browsedAt;
    
    // ==================== 领域行为 ====================
    
    /**
     * 检查浏览历史是否过期（超过30天）
     * 
     * @return 是否过期
     */
    public boolean isExpired() {
        if (browsedAt == null) {
            return true;
        }
        return browsedAt.isBefore(LocalDateTime.now().minusDays(30));
    }
    
    /**
     * 更新浏览时间
     */
    public void updateBrowsedTime() {
        this.browsedAt = LocalDateTime.now();
    }
}
