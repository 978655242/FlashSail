package com.flashsell.domain.history.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 热门关键词领域实体
 * 记录每日热门搜索关键词统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotKeyword {
    
    /**
     * 热门关键词ID
     */
    private Long id;
    
    /**
     * 关键词
     */
    private String keyword;
    
    /**
     * 搜索次数
     */
    private Integer searchCount;
    
    /**
     * 趋势：UP-上升, DOWN-下降, STABLE-稳定
     */
    private String trend;
    
    /**
     * 统计日期
     */
    private LocalDate statDate;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    // ==================== 领域行为 ====================
    
    /**
     * 增加搜索次数
     */
    public void incrementSearchCount() {
        if (this.searchCount == null) {
            this.searchCount = 1;
        } else {
            this.searchCount++;
        }
    }
    
    /**
     * 更新趋势
     * 
     * @param previousCount 前一天的搜索次数
     */
    public void updateTrend(Integer previousCount) {
        if (previousCount == null || this.searchCount == null) {
            this.trend = "STABLE";
            return;
        }
        
        double changeRate = (double) (this.searchCount - previousCount) / previousCount;
        
        if (changeRate > 0.1) {
            this.trend = "UP";
        } else if (changeRate < -0.1) {
            this.trend = "DOWN";
        } else {
            this.trend = "STABLE";
        }
    }
    
    /**
     * 检查是否为今日统计
     * 
     * @return 是否为今日
     */
    public boolean isToday() {
        return statDate != null && statDate.equals(LocalDate.now());
    }
}
