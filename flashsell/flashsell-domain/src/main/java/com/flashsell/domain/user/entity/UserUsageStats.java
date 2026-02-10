package com.flashsell.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户使用统计实体（按月统计）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUsageStats {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 统计月份（格式：YYYY-MM）
     */
    private String month;
    
    /**
     * 搜索次数
     */
    private Integer searchCount;
    
    /**
     * 导出次数
     */
    private Integer exportCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 增加搜索计数
     */
    public void incrementSearchCount() {
        this.searchCount = (this.searchCount == null ? 0 : this.searchCount) + 1;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 增加导出计数
     */
    public void incrementExportCount() {
        this.exportCount = (this.exportCount == null ? 0 : this.exportCount) + 1;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 检查是否可以搜索
     * 
     * @param limit 搜索限制（-1表示无限制）
     * @return 是否可以搜索
     */
    public boolean canSearch(int limit) {
        if (limit == -1) {
            return true;
        }
        return (this.searchCount == null ? 0 : this.searchCount) < limit;
    }
    
    /**
     * 检查是否可以导出
     * 
     * @param limit 导出限制（-1表示无限制）
     * @return 是否可以导出
     */
    public boolean canExport(int limit) {
        if (limit == -1) {
            return true;
        }
        return (this.exportCount == null ? 0 : this.exportCount) < limit;
    }
}
