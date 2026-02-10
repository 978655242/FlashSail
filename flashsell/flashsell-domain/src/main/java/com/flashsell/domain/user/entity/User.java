package com.flashsell.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户领域实体
 * 包含用户的核心属性和领域行为
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 密码哈希值
     */
    private String passwordHash;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
    
    /**
     * 邮箱地址
     */
    private String email;
    
    /**
     * 订阅等级
     */
    private SubscriptionLevel subscriptionLevel;
    
    /**
     * 订阅到期日期
     */
    private LocalDate subscriptionExpireDate;
    
    /**
     * 是否开启消息通知
     */
    private Boolean notificationEnabled;
    
    /**
     * 是否订阅邮件
     */
    private Boolean emailSubscribed;
    
    /**
     * 是否开启两步验证
     */
    private Boolean twoFactorEnabled;
    
    /**
     * TOTP两步验证密钥
     */
    private String twoFactorSecret;
    
    /**
     * 手机号是否已验证
     */
    private Boolean phoneVerified;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 删除时间（软删除）
     */
    private LocalDateTime deletedAt;
    
    // ==================== 领域行为 ====================
    
    /**
     * 检查用户是否可以创建新看板
     * 
     * @param currentBoardCount 当前看板数量
     * @return 是否可以创建
     */
    public boolean canCreateBoard(int currentBoardCount) {
        return currentBoardCount < getEffectiveSubscriptionLevel().getMaxBoards();
    }
    
    /**
     * 检查用户是否可以执行搜索
     * 
     * @param currentSearchCount 当前月搜索次数
     * @return 是否可以搜索
     */
    public boolean canSearch(int currentSearchCount) {
        SubscriptionLevel level = getEffectiveSubscriptionLevel();
        return level.hasUnlimitedSearches() || currentSearchCount < level.getMaxSearchesPerMonth();
    }
    
    /**
     * 检查用户是否可以导出报告
     * 
     * @param currentExportCount 当前月导出次数
     * @return 是否可以导出
     */
    public boolean canExport(int currentExportCount) {
        SubscriptionLevel level = getEffectiveSubscriptionLevel();
        return level.hasUnlimitedExports() || currentExportCount < level.getMaxExportsPerMonth();
    }
    
    /**
     * 检查用户是否可以添加收藏
     * 
     * @param currentFavoriteCount 当前收藏数量
     * @return 是否可以添加收藏
     */
    public boolean canAddFavorite(int currentFavoriteCount) {
        SubscriptionLevel level = getEffectiveSubscriptionLevel();
        return level.hasUnlimitedFavorites() || currentFavoriteCount < level.getMaxFavorites();
    }
    
    /**
     * 获取有效的订阅等级
     * 如果订阅已过期，返回 FREE 等级
     * 
     * @return 有效的订阅等级
     */
    public SubscriptionLevel getEffectiveSubscriptionLevel() {
        if (isSubscriptionExpired()) {
            return SubscriptionLevel.FREE;
        }
        return subscriptionLevel != null ? subscriptionLevel : SubscriptionLevel.FREE;
    }
    
    /**
     * 检查订阅是否已过期
     * 
     * @return 是否已过期
     */
    public boolean isSubscriptionExpired() {
        if (subscriptionLevel == SubscriptionLevel.FREE) {
            return false;
        }
        if (subscriptionExpireDate == null) {
            return true;
        }
        return LocalDate.now().isAfter(subscriptionExpireDate);
    }
    
    /**
     * 检查用户是否已被删除（软删除）
     * 
     * @return 是否已删除
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }
    
    /**
     * 更新最后登录时间
     */
    public void updateLastLoginTime() {
        this.lastLoginTime = LocalDateTime.now();
    }
    
    /**
     * 软删除用户
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
    
    /**
     * 升级订阅
     * 
     * @param level 新的订阅等级
     * @param expireDate 到期日期
     */
    public void upgradeSubscription(SubscriptionLevel level, LocalDate expireDate) {
        this.subscriptionLevel = level;
        this.subscriptionExpireDate = expireDate;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 降级到免费版
     */
    public void downgradeToFree() {
        this.subscriptionLevel = SubscriptionLevel.FREE;
        this.subscriptionExpireDate = null;
        this.updatedAt = LocalDateTime.now();
    }
}
