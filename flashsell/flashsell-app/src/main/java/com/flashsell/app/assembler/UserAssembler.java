package com.flashsell.app.assembler;

import com.flashsell.client.dto.res.*;
import com.flashsell.domain.user.entity.*;
import org.springframework.stereotype.Component;

/**
 * 用户DTO转换器
 * 负责领域实体与DTO之间的转换
 */
@Component
public class UserAssembler {
    
    /**
     * 转换为注册响应
     * 
     * @param user 用户实体
     * @param token 访问令牌
     * @param refreshToken 刷新令牌
     * @return 注册响应
     */
    public RegisterRes toRegisterRes(User user, String token, String refreshToken) {
        return RegisterRes.builder()
                .userId(user.getId())
                .token(token)
                .refreshToken(refreshToken)
                .build();
    }
    
    /**
     * 转换为登录响应
     * 
     * @param user 用户实体
     * @param token 访问令牌
     * @param refreshToken 刷新令牌
     * @return 登录响应
     */
    public LoginRes toLoginRes(User user, String token, String refreshToken) {
        return LoginRes.builder()
                .userId(user.getId())
                .token(token)
                .refreshToken(refreshToken)
                .userInfo(toUserInfo(user))
                .build();
    }
    
    /**
     * 转换为用户信息
     * 
     * @param user 用户实体
     * @return 用户信息
     */
    public LoginRes.UserInfo toUserInfo(User user) {
        return LoginRes.UserInfo.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .email(user.getEmail())
                .subscriptionLevel(user.getEffectiveSubscriptionLevel().name())
                .subscriptionExpireDate(user.getSubscriptionExpireDate())
                .lastLoginTime(user.getLastLoginTime())
                .build();
    }
    
    /**
     * 转换为用户资料响应
     * 
     * @param profile 用户资料值对象
     * @return 用户资料响应
     */
    public UserProfileRes toUserProfileRes(UserProfile profile) {
        return UserProfileRes.builder()
                .id(profile.getUserId())
                .phone(profile.getPhone())
                .nickname(profile.getNickname())
                .avatarUrl(profile.getAvatarUrl())
                .email(profile.getEmail())
                .subscriptionLevel(profile.getSubscriptionLevel().name())
                .subscriptionExpireDate(profile.getSubscriptionExpireDate())
                .phoneVerified(true)
                .twoFactorEnabled(false)
                .lastLoginTime(profile.getLastLoginTime())
                .createdAt(profile.getCreatedAt())
                .build();
    }
    
    /**
     * 转换为用户设置响应
     * 
     * @param settings 用户设置值对象
     * @return 用户设置响应
     */
    public UserSettingsRes toUserSettingsRes(UserSettings settings) {
        return UserSettingsRes.builder()
                .notificationEnabled(settings.getNotificationEnabled())
                .emailSubscribed(settings.getEmailSubscribed())
                .twoFactorEnabled(settings.getTwoFactorEnabled())
                .phoneVerified(settings.getPhoneVerified())
                .build();
    }
    
    /**
     * 转换为邀请信息响应
     * 
     * @param invite 用户邀请实体
     * @param baseUrl 基础URL
     * @return 邀请信息响应
     */
    public InviteInfoRes toInviteInfoRes(UserInvite invite, String baseUrl) {
        String inviteLink = baseUrl + "/register?inviteCode=" + invite.getInviteCode();
        
        return InviteInfoRes.builder()
                .inviteCode(invite.getInviteCode())
                .inviteLink(inviteLink)
                .invitedCount(invite.getInvitedCount())
                .rewardDays(invite.getRewardDays())
                .build();
    }
}
