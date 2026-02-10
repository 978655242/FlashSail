package com.flashsell.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户资料值对象
 * 封装用户的个人资料信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    
    /**
     * 用户ID
     */
    private Long userId;
    
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
     * 手机号
     */
    private String phone;
    
    /**
     * 订阅等级
     */
    private SubscriptionLevel subscriptionLevel;
    
    /**
     * 订阅到期日期
     */
    private LocalDate subscriptionExpireDate;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 从 User 实体创建 UserProfile
     * 
     * @param user 用户实体
     * @return 用户资料值对象
     */
    public static UserProfile fromUser(User user) {
        return UserProfile.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .email(user.getEmail())
                .phone(user.getPhone())
                .subscriptionLevel(user.getEffectiveSubscriptionLevel())
                .subscriptionExpireDate(user.getSubscriptionExpireDate())
                .lastLoginTime(user.getLastLoginTime())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
