package com.flashsell.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户设置值对象
 * 封装用户的偏好设置信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings {
    
    /**
     * 用户ID
     */
    private Long userId;
    
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
     * 手机号是否已验证
     */
    private Boolean phoneVerified;
    
    /**
     * 从 User 实体创建 UserSettings
     * 
     * @param user 用户实体
     * @return 用户设置值对象
     */
    public static UserSettings fromUser(User user) {
        return UserSettings.builder()
                .userId(user.getId())
                .notificationEnabled(user.getNotificationEnabled())
                .emailSubscribed(user.getEmailSubscribed())
                .twoFactorEnabled(user.getTwoFactorEnabled())
                .phoneVerified(user.getPhoneVerified())
                .build();
    }
}
