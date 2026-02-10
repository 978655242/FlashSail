package com.flashsell.domain.user.entity;

import lombok.Getter;

/**
 * 订阅等级枚举
 * 定义用户的订阅级别及其对应的权限限制
 */
@Getter
public enum SubscriptionLevel {
    
    /**
     * 免费版
     */
    FREE("免费版", 10, 50, 3, 10),
    
    /**
     * 基础版
     */
    BASIC("基础版", 30, 200, 10, 100),
    
    /**
     * 专业版
     */
    PRO("专业版", 50, -1, -1, -1);
    
    /**
     * 等级名称
     */
    private final String displayName;
    
    /**
     * 最大看板数量
     */
    private final int maxBoards;
    
    /**
     * 每月最大搜索次数，-1 表示无限制
     */
    private final int maxSearchesPerMonth;
    
    /**
     * 每月最大导出次数，-1 表示无限制
     */
    private final int maxExportsPerMonth;
    
    /**
     * 最大收藏数量，-1 表示无限制
     */
    private final int maxFavorites;
    
    SubscriptionLevel(String displayName, int maxBoards, int maxSearchesPerMonth, 
                      int maxExportsPerMonth, int maxFavorites) {
        this.displayName = displayName;
        this.maxBoards = maxBoards;
        this.maxSearchesPerMonth = maxSearchesPerMonth;
        this.maxExportsPerMonth = maxExportsPerMonth;
        this.maxFavorites = maxFavorites;
    }
    
    /**
     * 检查是否有无限搜索次数
     */
    public boolean hasUnlimitedSearches() {
        return maxSearchesPerMonth == -1;
    }
    
    /**
     * 检查是否有无限导出次数
     */
    public boolean hasUnlimitedExports() {
        return maxExportsPerMonth == -1;
    }
    
    /**
     * 检查是否有无限收藏数量
     */
    public boolean hasUnlimitedFavorites() {
        return maxFavorites == -1;
    }
}
