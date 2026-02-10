package com.flashsell.infrastructure.user.convertor;

import com.flashsell.domain.user.entity.SubscriptionLevel;
import com.flashsell.domain.user.entity.User;
import com.flashsell.infrastructure.user.dataobject.UserDO;
import org.springframework.stereotype.Component;

/**
 * 用户转换器
 * 负责 UserDO 和 User 领域实体之间的转换
 */
@Component
public class UserConvertor {

    /**
     * 将数据对象转换为领域实体
     *
     * @param userDO 用户数据对象
     * @return 用户领域实体
     */
    public User toEntity(UserDO userDO) {
        if (userDO == null) {
            return null;
        }

        return User.builder()
                .id(userDO.getId())
                .phone(userDO.getPhone())
                .passwordHash(userDO.getPasswordHash())
                .nickname(userDO.getNickname())
                .avatarUrl(userDO.getAvatarUrl())
                .email(userDO.getEmail())
                .subscriptionLevel(parseSubscriptionLevel(userDO.getSubscriptionLevel()))
                .subscriptionExpireDate(userDO.getSubscriptionExpireDate())
                .notificationEnabled(userDO.getNotificationEnabled())
                .emailSubscribed(userDO.getEmailSubscribed())
                .twoFactorEnabled(userDO.getTwoFactorEnabled())
                .twoFactorSecret(userDO.getTwoFactorSecret())
                .phoneVerified(userDO.getPhoneVerified())
                .lastLoginTime(userDO.getLastLoginTime())
                .createdAt(userDO.getCreatedAt())
                .updatedAt(userDO.getUpdatedAt())
                .deletedAt(userDO.getDeletedAt())
                .build();
    }

    /**
     * 将领域实体转换为数据对象
     *
     * @param user 用户领域实体
     * @return 用户数据对象
     */
    public UserDO toDataObject(User user) {
        if (user == null) {
            return null;
        }

        return UserDO.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .passwordHash(user.getPasswordHash())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .email(user.getEmail())
                .subscriptionLevel(formatSubscriptionLevel(user.getSubscriptionLevel()))
                .subscriptionExpireDate(user.getSubscriptionExpireDate())
                .notificationEnabled(user.getNotificationEnabled())
                .emailSubscribed(user.getEmailSubscribed())
                .twoFactorEnabled(user.getTwoFactorEnabled())
                .twoFactorSecret(user.getTwoFactorSecret())
                .phoneVerified(user.getPhoneVerified())
                .lastLoginTime(user.getLastLoginTime())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .deletedAt(user.getDeletedAt())
                .build();
    }

    /**
     * 解析订阅等级字符串为枚举
     *
     * @param level 订阅等级字符串
     * @return 订阅等级枚举
     */
    private SubscriptionLevel parseSubscriptionLevel(String level) {
        if (level == null || level.isEmpty()) {
            return SubscriptionLevel.FREE;
        }
        try {
            return SubscriptionLevel.valueOf(level);
        } catch (IllegalArgumentException e) {
            return SubscriptionLevel.FREE;
        }
    }

    /**
     * 格式化订阅等级枚举为字符串
     *
     * @param level 订阅等级枚举
     * @return 订阅等级字符串
     */
    private String formatSubscriptionLevel(SubscriptionLevel level) {
        if (level == null) {
            return SubscriptionLevel.FREE.name();
        }
        return level.name();
    }
}
