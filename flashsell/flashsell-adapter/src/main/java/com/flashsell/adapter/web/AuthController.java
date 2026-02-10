package com.flashsell.adapter.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flashsell.app.service.AuthAppService;
import com.flashsell.client.dto.ApiResponse;
import com.flashsell.client.dto.req.LoginReq;
import com.flashsell.client.dto.req.RefreshReq;
import com.flashsell.client.dto.req.RegisterReq;
import com.flashsell.client.dto.res.LoginRes;
import com.flashsell.client.dto.res.RefreshRes;
import com.flashsell.client.dto.res.RegisterRes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证控制器
 * 处理用户注册、登录、刷新令牌、登出等认证相关请求
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthAppService authAppService;
    
    /**
     * 用户注册
     * 
     * @param req 注册请求（手机号+验证码）
     * @return 注册响应（包含JWT令牌）
     */
    @PostMapping("/register")
    public ApiResponse<RegisterRes> register(@Valid @RequestBody RegisterReq req) {
        log.info("收到注册请求: phone={}", req.getPhone());
        RegisterRes res = authAppService.register(req);
        return ApiResponse.success(res);
    }
    
    /**
     * 用户登录
     * 
     * @param req 登录请求（手机号+验证码）
     * @return 登录响应（包含JWT令牌和用户信息）
     */
    @PostMapping("/login")
    public ApiResponse<LoginRes> login(@Valid @RequestBody LoginReq req) {
        log.info("收到登录请求: phone={}", req.getPhone());
        LoginRes res = authAppService.login(req);
        return ApiResponse.success(res);
    }
    
    /**
     * 刷新令牌
     * 
     * @param req 刷新令牌请求
     * @return 新的令牌对
     */
    @PostMapping("/refresh")
    public ApiResponse<RefreshRes> refresh(@Valid @RequestBody RefreshReq req) {
        log.info("收到刷新令牌请求");
        RefreshRes res = authAppService.refreshToken(req);
        return ApiResponse.success(res);
    }
    
    /**
     * 用户登出
     * 需要认证，从SecurityContext获取当前用户ID
     * 
     * @param userId 当前登录用户ID（由JWT过滤器注入）
     * @return 成功响应
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal Long userId) {
        log.info("收到登出请求: userId={}", userId);
        authAppService.logout(userId);
        return ApiResponse.success();
    }
    
    /**
     * 发送验证码
     * MVP阶段：模拟发送验证码，实际不发送短信
     * 生产环境：应集成短信服务商API
     * 
     * @param req 包含手机号的请求体
     * @return 成功响应
     */
    @PostMapping("/send-code")
    public ApiResponse<Void> sendCode(@RequestBody java.util.Map<String, String> req) {
        String phone = req.get("phone");
        log.info("收到发送验证码请求: phone={}", phone);
        
        // 验证手机号格式
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            return ApiResponse.error(400, "手机号格式不正确");
        }
        
        // MVP阶段：模拟发送成功，实际使用固定验证码 123456
        log.info("验证码发送成功（开发模式）: phone={}, code=123456", phone);
        return ApiResponse.success();
    }
}
