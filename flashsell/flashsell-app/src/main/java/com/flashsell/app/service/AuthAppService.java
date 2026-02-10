package com.flashsell.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flashsell.app.assembler.UserAssembler;
import com.flashsell.client.dto.req.LoginReq;
import com.flashsell.client.dto.req.RefreshReq;
import com.flashsell.client.dto.req.RegisterReq;
import com.flashsell.client.dto.res.LoginRes;
import com.flashsell.client.dto.res.RefreshRes;
import com.flashsell.client.dto.res.RegisterRes;
import com.flashsell.domain.user.entity.User;
import com.flashsell.domain.user.gateway.SessionGateway;
import com.flashsell.domain.user.gateway.TokenGateway;
import com.flashsell.domain.user.service.UserDomainService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证应用服务
 * 处理用户注册、登录、刷新令牌、登出等认证相关业务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthAppService {
    
    private final UserDomainService userDomainService;
    private final SessionGateway sessionGateway;
    private final TokenGateway tokenGateway;
    private final UserAssembler userAssembler;
    
    /**
     * 用户注册
     * 
     * @param req 注册请求
     * @return 注册响应（包含JWT令牌）
     * @throws IllegalArgumentException 如果手机号已存在或验证码无效
     */
    @Transactional
    public RegisterRes register(RegisterReq req) {
        log.info("用户注册: phone={}", req.getPhone());
        
        // 验证验证码（MVP阶段简化处理，实际应调用短信服务验证）
        validateVerifyCode(req.getPhone(), req.getVerifyCode());
        
        // 创建用户
        User user = userDomainService.createUser(req.getPhone());
        
        // 生成令牌
        String token = tokenGateway.generateAccessToken(user.getId());
        String refreshToken = tokenGateway.generateRefreshToken(user.getId());
        
        // 保存会话到Redis
        sessionGateway.saveSession(
                user.getId(), 
                token, 
                refreshToken, 
                tokenGateway.getRefreshTokenExpirationSeconds()
        );
        
        log.info("用户注册成功: userId={}", user.getId());
        return userAssembler.toRegisterRes(user, token, refreshToken);
    }
    
    /**
     * 用户登录
     * 
     * @param req 登录请求
     * @return 登录响应（包含JWT令牌和用户信息）
     * @throws IllegalArgumentException 如果用户不存在或验证码无效
     */
    @Transactional
    public LoginRes login(LoginReq req) {
        log.info("用户登录: phone={}", req.getPhone());
        
        // 验证验证码（MVP阶段简化处理）
        validateVerifyCode(req.getPhone(), req.getVerifyCode());
        
        // 查找用户
        User user = userDomainService.findByPhone(req.getPhone())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在，请先注册"));
        
        // 检查用户是否已被删除
        if (user.isDeleted()) {
            throw new IllegalArgumentException("账户已注销");
        }
        
        // 更新登录时间
        user = userDomainService.login(user);
        
        // 生成令牌
        String token = tokenGateway.generateAccessToken(user.getId());
        String refreshToken = tokenGateway.generateRefreshToken(user.getId());
        
        // 保存会话到Redis（会覆盖旧会话）
        sessionGateway.saveSession(
                user.getId(), 
                token, 
                refreshToken, 
                tokenGateway.getRefreshTokenExpirationSeconds()
        );
        
        log.info("用户登录成功: userId={}", user.getId());
        return userAssembler.toLoginRes(user, token, refreshToken);
    }
    
    /**
     * 刷新令牌
     * 
     * @param req 刷新令牌请求
     * @return 新的令牌对
     * @throws IllegalArgumentException 如果刷新令牌无效或已过期
     */
    public RefreshRes refreshToken(RefreshReq req) {
        String refreshToken = req.getRefreshToken();
        
        // 验证刷新令牌格式
        if (!tokenGateway.validateToken(refreshToken)) {
            throw new IllegalArgumentException("无效的刷新令牌");
        }
        
        // 验证是否为刷新令牌类型
        if (!tokenGateway.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("令牌类型错误");
        }
        
        // 获取用户ID
        Long userId = tokenGateway.getUserIdFromToken(refreshToken);
        
        // 验证Redis中的刷新令牌
        if (!sessionGateway.validateRefreshToken(userId, refreshToken)) {
            throw new IllegalArgumentException("刷新令牌已失效，请重新登录");
        }
        
        // 生成新的令牌对
        String newToken = tokenGateway.generateAccessToken(userId);
        String newRefreshToken = tokenGateway.generateRefreshToken(userId);
        
        // 更新Redis中的会话
        sessionGateway.updateSession(
                userId, 
                newToken, 
                newRefreshToken, 
                tokenGateway.getRefreshTokenExpirationSeconds()
        );
        
        log.info("令牌刷新成功: userId={}", userId);
        return RefreshRes.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .build();
    }
    
    /**
     * 用户登出
     * 
     * @param userId 用户ID
     */
    public void logout(Long userId) {
        log.info("用户登出: userId={}", userId);
        
        // 使Redis中的会话失效
        sessionGateway.invalidateSession(userId);
        
        log.info("用户登出成功: userId={}", userId);
    }
    
    /**
     * 验证访问令牌
     * 
     * @param token 访问令牌
     * @return 用户ID（如果令牌有效）
     * @throws IllegalArgumentException 如果令牌无效
     */
    public Long validateAccessToken(String token) {
        // 验证令牌格式
        if (!tokenGateway.validateToken(token)) {
            throw new IllegalArgumentException("无效的访问令牌");
        }
        
        // 验证是否为访问令牌类型
        if (!tokenGateway.isAccessToken(token)) {
            throw new IllegalArgumentException("令牌类型错误");
        }
        
        // 获取用户ID
        Long userId = tokenGateway.getUserIdFromToken(token);
        
        // 验证会话是否存在（用户是否已登出）
        if (!sessionGateway.hasSession(userId)) {
            throw new IllegalArgumentException("会话已失效，请重新登录");
        }
        
        return userId;
    }
    
    /**
     * 验证验证码
     * 开发模式：接受固定验证码 "123456" 或任何6位数字
     * 生产环境：应调用短信服务验证
     *
     * @param phone 手机号
     * @param verifyCode 验证码
     */
    private void validateVerifyCode(String phone, String verifyCode) {
        // 验证手机号格式
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("手机号格式不正确");
        }

        // 开发环境：接受任何6位数字验证码或固定的 "123456"
        if (verifyCode != null && verifyCode.matches("^\\d{6}$")) {
            log.info("验证码验证通过（开发模式）: phone={}, code={}", phone, verifyCode);
            return;
        }

        throw new IllegalArgumentException("验证码不能为空且必须为6位数字");
    }
}
