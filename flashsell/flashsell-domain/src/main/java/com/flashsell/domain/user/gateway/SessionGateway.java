package com.flashsell.domain.user.gateway;

import java.util.Optional;

/**
 * 会话网关接口
 * 定义用户会话管理的抽象接口，由 infrastructure 层实现
 * 用于 Redis 中存储和管理用户会话
 */
public interface SessionGateway {
    
    /**
     * 保存用户会话
     * 
     * @param userId 用户ID
     * @param token JWT访问令牌
     * @param refreshToken 刷新令牌
     * @param ttlSeconds 过期时间（秒）
     */
    void saveSession(Long userId, String token, String refreshToken, long ttlSeconds);
    
    /**
     * 获取用户会话的访问令牌
     * 
     * @param userId 用户ID
     * @return 访问令牌（可能为空）
     */
    Optional<String> getToken(Long userId);
    
    /**
     * 获取用户会话的刷新令牌
     * 
     * @param userId 用户ID
     * @return 刷新令牌（可能为空）
     */
    Optional<String> getRefreshToken(Long userId);
    
    /**
     * 验证刷新令牌是否有效
     * 
     * @param userId 用户ID
     * @param refreshToken 刷新令牌
     * @return 是否有效
     */
    boolean validateRefreshToken(Long userId, String refreshToken);
    
    /**
     * 使用户会话失效（登出）
     * 
     * @param userId 用户ID
     */
    void invalidateSession(Long userId);
    
    /**
     * 检查用户会话是否存在
     * 
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean hasSession(Long userId);
    
    /**
     * 更新会话的访问令牌
     * 
     * @param userId 用户ID
     * @param newToken 新的访问令牌
     * @param newRefreshToken 新的刷新令牌
     * @param ttlSeconds 过期时间（秒）
     */
    void updateSession(Long userId, String newToken, String newRefreshToken, long ttlSeconds);
}
