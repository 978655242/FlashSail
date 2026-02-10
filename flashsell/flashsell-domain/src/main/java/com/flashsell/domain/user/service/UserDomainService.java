package com.flashsell.domain.user.service;

import com.flashsell.domain.user.entity.*;
import com.flashsell.domain.user.gateway.UserGateway;
import com.flashsell.domain.user.gateway.UserInviteGateway;
import com.flashsell.domain.user.gateway.UserUsageStatsGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;

/**
 * 用户领域服务
 * 处理用户相关的核心业务逻辑
 */
@Service
@RequiredArgsConstructor
public class UserDomainService {
    
    private final UserGateway userGateway;
    private final UserInviteGateway userInviteGateway;
    private final UserUsageStatsGateway userUsageStatsGateway;
    
    /**
     * 创建新用户（注册）
     * 
     * @param phone 手机号
     * @return 创建的用户实体
     * @throws IllegalArgumentException 如果手机号已存在
     */
    public User createUser(String phone) {
        // 检查手机号是否已存在
        if (userGateway.existsByPhone(phone)) {
            throw new IllegalArgumentException("手机号已被注册");
        }
        
        // 创建新用户，默认为免费版
        User user = User.builder()
                .phone(phone)
                .subscriptionLevel(SubscriptionLevel.FREE)
                .notificationEnabled(true)
                .emailSubscribed(false)
                .twoFactorEnabled(false)
                .phoneVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        return userGateway.save(user);
    }
    
    /**
     * 根据手机号查找用户
     * 
     * @param phone 手机号
     * @return 用户实体（可能为空）
     */
    public Optional<User> findByPhone(String phone) {
        return userGateway.findByPhone(phone);
    }
    
    /**
     * 根据ID查找用户
     * 
     * @param userId 用户ID
     * @return 用户实体（可能为空）
     */
    public Optional<User> findById(Long userId) {
        return userGateway.findById(userId);
    }
    
    /**
     * 用户登录
     * 更新最后登录时间
     * 
     * @param user 用户实体
     * @return 更新后的用户实体
     */
    public User login(User user) {
        user.updateLastLoginTime();
        userGateway.update(user);
        return user;
    }
    
    /**
     * 更新用户资料
     * 
     * @param userId 用户ID
     * @param nickname 昵称
     * @param avatarUrl 头像URL
     * @return 更新后的用户实体
     * @throws IllegalArgumentException 如果用户不存在
     */
    public User updateProfile(Long userId, String nickname, String avatarUrl) {
        User user = userGateway.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }
        user.setUpdatedAt(LocalDateTime.now());
        
        userGateway.update(user);
        return user;
    }
    
    /**
     * 更新用户设置
     * 
     * @param userId 用户ID
     * @param notificationEnabled 是否开启通知
     * @param emailSubscribed 是否订阅邮件
     * @return 更新后的用户实体
     * @throws IllegalArgumentException 如果用户不存在
     */
    public User updateSettings(Long userId, Boolean notificationEnabled, Boolean emailSubscribed) {
        User user = userGateway.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        if (notificationEnabled != null) {
            user.setNotificationEnabled(notificationEnabled);
        }
        if (emailSubscribed != null) {
            user.setEmailSubscribed(emailSubscribed);
        }
        user.setUpdatedAt(LocalDateTime.now());
        
        userGateway.update(user);
        return user;
    }
    
    /**
     * 升级用户订阅
     * 
     * @param userId 用户ID
     * @param level 订阅等级
     * @param expireDate 到期日期
     * @return 更新后的用户实体
     * @throws IllegalArgumentException 如果用户不存在
     */
    public User upgradeSubscription(Long userId, SubscriptionLevel level, LocalDate expireDate) {
        User user = userGateway.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        user.upgradeSubscription(level, expireDate);
        userGateway.update(user);
        return user;
    }
    
    /**
     * 检查并降级过期订阅
     * 
     * @param userId 用户ID
     * @return 如果发生降级返回 true
     */
    public boolean checkAndDowngradeExpiredSubscription(Long userId) {
        Optional<User> userOpt = userGateway.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        if (user.isSubscriptionExpired() && user.getSubscriptionLevel() != SubscriptionLevel.FREE) {
            user.downgradeToFree();
            userGateway.update(user);
            return true;
        }
        return false;
    }
    
    /**
     * 启用两步验证
     * 
     * @param userId 用户ID
     * @param secret TOTP密钥
     * @return 更新后的用户实体
     * @throws IllegalArgumentException 如果用户不存在
     */
    public User enableTwoFactor(Long userId, String secret) {
        User user = userGateway.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        user.setTwoFactorEnabled(true);
        user.setTwoFactorSecret(secret);
        user.setUpdatedAt(LocalDateTime.now());
        
        userGateway.update(user);
        return user;
    }
    
    /**
     * 禁用两步验证
     * 
     * @param userId 用户ID
     * @return 更新后的用户实体
     * @throws IllegalArgumentException 如果用户不存在
     */
    public User disableTwoFactor(Long userId) {
        User user = userGateway.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        user.setUpdatedAt(LocalDateTime.now());
        
        userGateway.update(user);
        return user;
    }
    
    /**
     * 注销用户账户（软删除）
     * 
     * @param userId 用户ID
     * @throws IllegalArgumentException 如果用户不存在
     */
    public void deleteAccount(Long userId) {
        User user = userGateway.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        user.softDelete();
        userGateway.softDelete(userId);
    }
    
    /**
     * 绑定邮箱
     * 
     * @param userId 用户ID
     * @param email 邮箱地址
     * @return 更新后的用户实体
     * @throws IllegalArgumentException 如果用户不存在或邮箱已被使用
     */
    public User bindEmail(Long userId, String email) {
        User user = userGateway.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        if (userGateway.existsByEmail(email)) {
            throw new IllegalArgumentException("邮箱已被使用");
        }
        
        user.setEmail(email);
        user.setUpdatedAt(LocalDateTime.now());
        
        userGateway.update(user);
        return user;
    }
    
    /**
     * 更新密码
     * 
     * @param userId 用户ID
     * @param passwordHash 新密码哈希值
     * @return 更新后的用户实体
     * @throws IllegalArgumentException 如果用户不存在
     */
    public User updatePassword(Long userId, String passwordHash) {
        User user = userGateway.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        user.setPasswordHash(passwordHash);
        user.setUpdatedAt(LocalDateTime.now());
        
        userGateway.update(user);
        return user;
    }
    
    /**
     * 修改密码（需要验证原密码）
     * 
     * @param userId 用户ID
     * @param oldPasswordHash 原密码哈希值
     * @param newPasswordHash 新密码哈希值
     * @return 更新后的用户实体
     * @throws IllegalArgumentException 如果用户不存在或原密码错误
     */
    public User changePassword(Long userId, String oldPasswordHash, String newPasswordHash) {
        User user = userGateway.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        // 验证原密码
        if (!oldPasswordHash.equals(user.getPasswordHash())) {
            throw new IllegalArgumentException("原密码错误");
        }
        
        user.setPasswordHash(newPasswordHash);
        user.setUpdatedAt(LocalDateTime.now());
        
        userGateway.update(user);
        return user;
    }
    
    /**
     * 绑定手机号
     * 
     * @param userId 用户ID
     * @param phone 手机号
     * @return 更新后的用户实体
     * @throws IllegalArgumentException 如果用户不存在或手机号已被使用
     */
    public User bindPhone(Long userId, String phone) {
        User user = userGateway.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        if (userGateway.existsByPhone(phone)) {
            throw new IllegalArgumentException("手机号已被使用");
        }
        
        user.setPhone(phone);
        user.setPhoneVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        
        userGateway.update(user);
        return user;
    }
    
    /**
     * 获取用户资料
     * 
     * @param userId 用户ID
     * @return 用户资料值对象
     * @throws IllegalArgumentException 如果用户不存在
     */
    public com.flashsell.domain.user.entity.UserProfile getUserProfile(Long userId) {
        User user = userGateway.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        return com.flashsell.domain.user.entity.UserProfile.fromUser(user);
    }
    
    /**
     * 获取用户设置
     * 
     * @param userId 用户ID
     * @return 用户设置值对象
     * @throws IllegalArgumentException 如果用户不存在
     */
    public com.flashsell.domain.user.entity.UserSettings getUserSettings(Long userId) {
        User user = userGateway.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        return com.flashsell.domain.user.entity.UserSettings.fromUser(user);
    }
    
    /**
     * 获取或创建用户邀请信息
     * 
     * @param userId 用户ID
     * @return 用户邀请信息
     */
    public UserInvite getOrCreateUserInvite(Long userId) {
        Optional<UserInvite> inviteOpt = userInviteGateway.findByUserId(userId);
        if (inviteOpt.isPresent()) {
            return inviteOpt.get();
        }
        
        // 生成唯一邀请码
        String inviteCode = generateUniqueInviteCode();
        
        UserInvite invite = UserInvite.builder()
                .userId(userId)
                .inviteCode(inviteCode)
                .invitedCount(0)
                .rewardDays(0)
                .createdAt(LocalDateTime.now())
                .build();
        
        return userInviteGateway.save(invite);
    }
    
    /**
     * 处理邀请注册
     * 
     * @param inviteCode 邀请码
     * @param newUserId 新用户ID
     * @return 是否处理成功
     */
    public boolean processInviteRegistration(String inviteCode, Long newUserId) {
        Optional<UserInvite> inviteOpt = userInviteGateway.findByInviteCode(inviteCode);
        if (inviteOpt.isEmpty()) {
            return false;
        }
        
        UserInvite invite = inviteOpt.get();
        invite.incrementInviteCount(7); // 每次邀请奖励7天
        userInviteGateway.update(invite);
        
        return true;
    }
    
    /**
     * 获取当月使用统计
     * 
     * @param userId 用户ID
     * @return 使用统计
     */
    public UserUsageStats getCurrentMonthUsageStats(Long userId) {
        String currentMonth = YearMonth.now().toString();
        return userUsageStatsGateway.getOrCreate(userId, currentMonth);
    }
    
    /**
     * 记录搜索操作
     * 
     * @param userId 用户ID
     */
    public void recordSearch(Long userId) {
        String currentMonth = YearMonth.now().toString();
        UserUsageStats stats = userUsageStatsGateway.getOrCreate(userId, currentMonth);
        stats.incrementSearchCount();
        userUsageStatsGateway.update(stats);
    }
    
    /**
     * 记录导出操作
     * 
     * @param userId 用户ID
     */
    public void recordExport(Long userId) {
        String currentMonth = YearMonth.now().toString();
        UserUsageStats stats = userUsageStatsGateway.getOrCreate(userId, currentMonth);
        stats.incrementExportCount();
        userUsageStatsGateway.update(stats);
    }
    
    /**
     * 生成唯一邀请码
     * 
     * @return 邀请码
     */
    private String generateUniqueInviteCode() {
        String inviteCode;
        do {
            // 生成8位随机邀请码
            inviteCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (userInviteGateway.existsByInviteCode(inviteCode));
        
        return inviteCode;
    }
}
