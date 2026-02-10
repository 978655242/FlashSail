package com.flashsell.adapter.web;

import com.flashsell.app.service.UserAppService;
import com.flashsell.client.dto.ApiResponse;
import com.flashsell.client.dto.req.*;
import com.flashsell.client.dto.res.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 处理个人中心相关的API请求
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserAppService userAppService;
    
    /**
     * 获取用户资料
     * 
     * @param authentication 认证信息
     * @return 用户资料响应
     */
    @GetMapping("/profile")
    public ApiResponse<UserProfileRes> getProfile(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("GET /api/user/profile - userId={}", userId);
        
        UserProfileRes profile = userAppService.getProfile(userId);
        return ApiResponse.success(profile);
    }
    
    /**
     * 更新用户资料
     * 
     * @param authentication 认证信息
     * @param req 更新请求
     * @return 更新后的用户资料
     */
    @PutMapping("/profile")
    public ApiResponse<UserProfileRes> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileReq req) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("PUT /api/user/profile - userId={}", userId);
        
        UserProfileRes profile = userAppService.updateProfile(userId, req);
        return ApiResponse.success(profile);
    }
    
    /**
     * 获取使用情况统计
     * 
     * @param authentication 认证信息
     * @return 使用统计响应
     */
    @GetMapping("/usage")
    public ApiResponse<UserUsageStatsRes> getUsage(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("GET /api/user/usage - userId={}", userId);
        
        UserUsageStatsRes stats = userAppService.getUsageStats(userId);
        return ApiResponse.success(stats);
    }
    
    /**
     * 获取用户设置
     * 
     * @param authentication 认证信息
     * @return 用户设置响应
     */
    @GetMapping("/settings")
    public ApiResponse<UserSettingsRes> getSettings(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("GET /api/user/settings - userId={}", userId);
        
        UserSettingsRes settings = userAppService.getSettings(userId);
        return ApiResponse.success(settings);
    }
    
    /**
     * 更新用户设置
     * 
     * @param authentication 认证信息
     * @param req 更新请求
     * @return 更新后的用户设置
     */
    @PutMapping("/settings")
    public ApiResponse<UserSettingsRes> updateSettings(
            Authentication authentication,
            @Valid @RequestBody UpdateSettingsReq req) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("PUT /api/user/settings - userId={}", userId);
        
        UserSettingsRes settings = userAppService.updateSettings(userId, req);
        return ApiResponse.success(settings);
    }
    
    /**
     * 修改密码
     * 
     * @param authentication 认证信息
     * @param req 修改密码请求
     * @return 成功响应
     */
    @PostMapping("/password")
    public ApiResponse<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordReq req) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("POST /api/user/password - userId={}", userId);
        
        userAppService.changePassword(userId, req);
        return ApiResponse.success(null);
    }
    
    /**
     * 发送绑定手机验证码
     * 
     * @param authentication 认证信息
     * @param phone 手机号
     * @return 成功响应
     */
    @PostMapping("/phone/bind-code")
    public ApiResponse<Void> sendBindCode(
            Authentication authentication,
            @RequestParam String phone) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("POST /api/user/phone/bind-code - userId={}, phone={}", userId, phone);
        
        // TODO: 实际应调用短信服务发送验证码
        log.info("验证码发送成功（MVP模式）");
        return ApiResponse.success(null);
    }
    
    /**
     * 绑定手机号
     * 
     * @param authentication 认证信息
     * @param req 绑定请求
     * @return 成功响应
     */
    @PostMapping("/phone/bind")
    public ApiResponse<Void> bindPhone(
            Authentication authentication,
            @Valid @RequestBody BindPhoneReq req) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("POST /api/user/phone/bind - userId={}", userId);
        
        userAppService.bindPhone(userId, req);
        return ApiResponse.success(null);
    }
    
    /**
     * 开启两步验证
     * 
     * @param authentication 认证信息
     * @return 两步验证信息（包含密钥和二维码）
     */
    @PostMapping("/2fa/enable")
    public ApiResponse<Enable2FARes> enable2FA(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("POST /api/user/2fa/enable - userId={}", userId);
        
        Enable2FARes res = userAppService.enable2FA(userId);
        return ApiResponse.success(res);
    }
    
    /**
     * 验证两步验证码
     * 
     * @param authentication 认证信息
     * @param req 验证请求
     * @return 验证结果
     */
    @PostMapping("/2fa/verify")
    public ApiResponse<Boolean> verify2FA(
            Authentication authentication,
            @Valid @RequestBody Verify2FAReq req) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("POST /api/user/2fa/verify - userId={}", userId);
        
        boolean valid = userAppService.verify2FA(userId, req);
        return ApiResponse.success(valid);
    }
    
    /**
     * 禁用两步验证
     * 
     * @param authentication 认证信息
     * @return 成功响应
     */
    @PostMapping("/2fa/disable")
    public ApiResponse<Void> disable2FA(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("POST /api/user/2fa/disable - userId={}", userId);
        
        userAppService.disable2FA(userId);
        return ApiResponse.success(null);
    }
    
    /**
     * 注销账户
     * 
     * @param authentication 认证信息
     * @param req 注销请求
     * @return 成功响应
     */
    @DeleteMapping("/account")
    public ApiResponse<Void> deleteAccount(
            Authentication authentication,
            @Valid @RequestBody DeleteAccountReq req) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("DELETE /api/user/account - userId={}", userId);
        
        userAppService.deleteAccount(userId, req);
        return ApiResponse.success(null);
    }
    
    /**
     * 获取邀请信息
     *
     * @param authentication 认证信息
     * @return 邀请信息响应
     */
    @GetMapping("/invite")
    public ApiResponse<InviteInfoRes> getInviteInfo(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("GET /api/user/invite - userId={}", userId);

        InviteInfoRes invite = userAppService.getInviteInfo(userId);
        return ApiResponse.success(invite);
    }

    /**
     * 获取邀请码（兼容前端调用）
     *
     * @param authentication 认证信息
     * @return 邀请码响应
     */
    @GetMapping("/invite-code")
    public ApiResponse<InviteInfoRes> getInviteCode(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("GET /api/user/invite-code - userId={}", userId);

        InviteInfoRes invite = userAppService.getInviteInfo(userId);
        return ApiResponse.success(invite);
    }
    
    /**
     * 导出数据报告
     * 
     * @param authentication 认证信息
     * @param req 导出请求
     * @return 导出响应（包含下载链接）
     */
    @PostMapping("/export")
    public ApiResponse<ExportReportRes> exportReport(
            Authentication authentication,
            @Valid @RequestBody ExportReportReq req) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("POST /api/user/export - userId={}, type={}", userId, req.getType());
        
        ExportReportRes res = userAppService.exportReport(userId, req);
        return ApiResponse.success(res);
    }
}
