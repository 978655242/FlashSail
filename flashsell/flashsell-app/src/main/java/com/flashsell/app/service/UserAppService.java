package com.flashsell.app.service;

import com.flashsell.app.assembler.UserAssembler;
import com.flashsell.client.dto.req.*;
import com.flashsell.client.dto.res.*;
import com.flashsell.domain.user.entity.*;
import com.flashsell.domain.user.gateway.SessionGateway;
import com.flashsell.domain.user.service.UserDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户应用服务
 * 处理用户资料、设置、安全、邀请等个人中心相关业务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAppService {
    
    private final UserDomainService userDomainService;
    private final UserAssembler userAssembler;
    private final PasswordEncoder passwordEncoder;
    private final SessionGateway sessionGateway;
    
    @Value("${flashsell.base-url:http://localhost:8080}")
    private String baseUrl;
    
    /**
     * 获取用户资料
     * 
     * @param userId 用户ID
     * @return 用户资料响应
     */
    public UserProfileRes getProfile(Long userId) {
        log.info("获取用户资料: userId={}", userId);
        
        UserProfile profile = userDomainService.getUserProfile(userId);
        return userAssembler.toUserProfileRes(profile);
    }
    
    /**
     * 更新用户资料
     * 
     * @param userId 用户ID
     * @param req 更新请求
     * @return 更新后的用户资料
     */
    @Transactional
    public UserProfileRes updateProfile(Long userId, UpdateProfileReq req) {
        log.info("更新用户资料: userId={}, nickname={}", userId, req.getNickname());
        
        // 更新用户资料
        User user = userDomainService.updateProfile(userId, req.getNickname(), req.getAvatarUrl());
        
        // 如果更新了邮箱，单独处理
        if (req.getEmail() != null && !req.getEmail().equals(user.getEmail())) {
            user = userDomainService.bindEmail(userId, req.getEmail());
        }
        
        UserProfile profile = UserProfile.fromUser(user);
        return userAssembler.toUserProfileRes(profile);
    }
    
    /**
     * 获取使用情况统计
     * 
     * @param userId 用户ID
     * @return 使用统计响应
     */
    public UserUsageStatsRes getUsageStats(Long userId) {
        log.info("获取使用统计: userId={}", userId);
        
        // 获取当月使用统计
        UserUsageStats stats = userDomainService.getCurrentMonthUsageStats(userId);
        
        // 获取用户订阅等级
        User user = userDomainService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        SubscriptionLevel level = user.getEffectiveSubscriptionLevel();
        
        // TODO: 获取实际的收藏数和看板数（需要调用相应的服务）
        int favoriteCount = 0;
        int boardCount = 0;
        
        return UserUsageStatsRes.builder()
                .searchCount(stats.getSearchCount() != null ? stats.getSearchCount() : 0)
                .exportCount(stats.getExportCount() != null ? stats.getExportCount() : 0)
                .favoriteCount(favoriteCount)
                .boardCount(boardCount)
                .searchLimit(level.getMaxSearchesPerMonth())
                .exportLimit(level.getMaxExportsPerMonth())
                .boardLimit(level.getMaxBoards())
                .favoriteLimit(level.getMaxFavorites())
                .build();
    }
    
    /**
     * 获取用户设置
     * 
     * @param userId 用户ID
     * @return 用户设置响应
     */
    public UserSettingsRes getSettings(Long userId) {
        log.info("获取用户设置: userId={}", userId);
        
        UserSettings settings = userDomainService.getUserSettings(userId);
        return userAssembler.toUserSettingsRes(settings);
    }
    
    /**
     * 更新用户设置
     * 
     * @param userId 用户ID
     * @param req 更新请求
     * @return 更新后的用户设置
     */
    @Transactional
    public UserSettingsRes updateSettings(Long userId, UpdateSettingsReq req) {
        log.info("更新用户设置: userId={}", userId);
        
        User user = userDomainService.updateSettings(
                userId, 
                req.getNotificationEnabled(), 
                req.getEmailSubscribed()
        );
        
        UserSettings settings = UserSettings.fromUser(user);
        return userAssembler.toUserSettingsRes(settings);
    }
    
    /**
     * 修改密码
     * 
     * @param userId 用户ID
     * @param req 修改密码请求
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordReq req) {
        log.info("修改密码: userId={}", userId);
        
        // 加密原密码和新密码
        String oldPasswordHash = passwordEncoder.encode(req.getOldPassword());
        String newPasswordHash = passwordEncoder.encode(req.getNewPassword());
        
        // 修改密码
        userDomainService.changePassword(userId, oldPasswordHash, newPasswordHash);
        
        // 使所有会话失效
        sessionGateway.invalidateSession(userId);
        
        log.info("密码修改成功，所有会话已失效: userId={}", userId);
    }
    
    /**
     * 绑定手机号
     * 
     * @param userId 用户ID
     * @param req 绑定请求
     */
    @Transactional
    public void bindPhone(Long userId, BindPhoneReq req) {
        log.info("绑定手机号: userId={}, phone={}", userId, req.getPhone());
        
        // 验证验证码（MVP阶段简化处理）
        validateVerifyCode(req.getPhone(), req.getVerifyCode());
        
        // 绑定手机号
        userDomainService.bindPhone(userId, req.getPhone());
        
        log.info("手机号绑定成功: userId={}", userId);
    }
    
    /**
     * 开启两步验证
     * 
     * @param userId 用户ID
     * @return 两步验证信息（包含密钥和二维码）
     */
    @Transactional
    public Enable2FARes enable2FA(Long userId) {
        log.info("开启两步验证: userId={}", userId);
        
        // 生成TOTP密钥
        String secret = generateTOTPSecret();
        
        // 启用两步验证
        userDomainService.enableTwoFactor(userId, secret);
        
        // 生成二维码URL（实际应使用TOTP库生成）
        String qrCodeUrl = generateQRCodeUrl(userId, secret);
        
        log.info("两步验证开启成功: userId={}", userId);
        return Enable2FARes.builder()
                .secret(secret)
                .qrCodeUrl(qrCodeUrl)
                .build();
    }
    
    /**
     * 验证两步验证码
     * 
     * @param userId 用户ID
     * @param req 验证请求
     * @return 是否验证成功
     */
    public boolean verify2FA(Long userId, Verify2FAReq req) {
        log.info("验证两步验证码: userId={}", userId);
        
        // TODO: 实际应使用TOTP库验证
        // MVP阶段简化处理，接受任意6位数字
        boolean valid = req.getCode().matches("^\\d{6}$");
        
        log.info("两步验证码验证结果: userId={}, valid={}", userId, valid);
        return valid;
    }
    
    /**
     * 禁用两步验证
     * 
     * @param userId 用户ID
     */
    @Transactional
    public void disable2FA(Long userId) {
        log.info("禁用两步验证: userId={}", userId);
        
        userDomainService.disableTwoFactor(userId);
        
        log.info("两步验证已禁用: userId={}", userId);
    }
    
    /**
     * 注销账户
     * 
     * @param userId 用户ID
     * @param req 注销请求
     */
    @Transactional
    public void deleteAccount(Long userId, DeleteAccountReq req) {
        log.info("注销账户: userId={}, reason={}", userId, req.getReason());
        
        // 验证密码
        User user = userDomainService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        String passwordHash = passwordEncoder.encode(req.getPassword());
        if (!passwordHash.equals(user.getPasswordHash())) {
            throw new IllegalArgumentException("密码错误");
        }
        
        // 软删除用户
        userDomainService.deleteAccount(userId);
        
        // 使所有会话失效
        sessionGateway.invalidateSession(userId);
        
        log.info("账户注销成功: userId={}", userId);
    }
    
    /**
     * 获取邀请信息
     * 
     * @param userId 用户ID
     * @return 邀请信息响应
     */
    public InviteInfoRes getInviteInfo(Long userId) {
        log.info("获取邀请信息: userId={}", userId);
        
        UserInvite invite = userDomainService.getOrCreateUserInvite(userId);
        return userAssembler.toInviteInfoRes(invite, baseUrl);
    }
    
    /**
     * 导出数据报告
     * 
     * @param userId 用户ID
     * @param req 导出请求
     * @return 导出响应（包含下载链接）
     */
    @Transactional
    public ExportReportRes exportReport(Long userId, ExportReportReq req) {
        log.info("导出数据报告: userId={}, type={}", userId, req.getType());
        
        // 记录导出操作
        userDomainService.recordExport(userId);
        
        // TODO: 实际应生成PDF报告并上传到OSS
        // MVP阶段返回模拟URL
        String downloadUrl = baseUrl + "/api/user/reports/" + UUID.randomUUID().toString() + ".pdf";
        LocalDateTime expireTime = LocalDateTime.now().plusHours(24);
        
        log.info("报告导出成功: userId={}, downloadUrl={}", userId, downloadUrl);
        return ExportReportRes.builder()
                .downloadUrl(downloadUrl)
                .expireTime(expireTime)
                .build();
    }
    
    /**
     * 验证验证码
     * MVP阶段简化处理
     * 
     * @param phone 手机号
     * @param verifyCode 验证码
     */
    private void validateVerifyCode(String phone, String verifyCode) {
        if (verifyCode == null || !verifyCode.matches("^\\d{6}$")) {
            throw new IllegalArgumentException("验证码格式不正确");
        }
        log.debug("验证码验证通过（MVP模式）: phone={}", phone);
    }
    
    /**
     * 生成TOTP密钥
     * 
     * @return TOTP密钥
     */
    private String generateTOTPSecret() {
        // TODO: 实际应使用TOTP库生成
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
    
    /**
     * 生成二维码URL
     * 
     * @param userId 用户ID
     * @param secret TOTP密钥
     * @return 二维码URL
     */
    private String generateQRCodeUrl(Long userId, String secret) {
        // TODO: 实际应使用TOTP库生成二维码
        String otpauth = String.format("otpauth://totp/FlashSell:%d?secret=%s&issuer=FlashSell", 
                userId, secret);
        return baseUrl + "/api/user/2fa/qrcode?data=" + otpauth;
    }
}
