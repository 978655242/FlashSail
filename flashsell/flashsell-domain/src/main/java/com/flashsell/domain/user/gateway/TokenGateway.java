package com.flashsell.domain.user.gateway;

import com.flashsell.domain.user.entity.UserRole;

/**
 * Token网关接口
 * 定义JWT令牌相关操作
 */
public interface TokenGateway {

    /**
     * 生成访问令牌
     *
     * @param userId 用户ID
     * @return JWT访问令牌
     */
    String generateAccessToken(Long userId);

    /**
     * 生成带角色的访问令牌
     *
     * @param userId 用户ID
     * @param role 用户角色
     * @return JWT访问令牌
     */
    String generateAccessToken(Long userId, UserRole role);

    /**
     * 生成刷新令牌
     *
     * @param userId 用户ID
     * @return 刷新令牌
     */
    String generateRefreshToken(Long userId);

    /**
     * 从令牌中获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    Long getUserIdFromToken(String token);

    /**
     * 从令牌中获取用户角色
     *
     * @param token JWT令牌
     * @return 用户角色
     */
    UserRole getRoleFromToken(String token);

    /**
     * 验证令牌是否有效
     *
     * @param token JWT令牌
     * @return 是否有效
     */
    boolean validateToken(String token);

    /**
     * 检查令牌是否为访问令牌
     *
     * @param token JWT令牌
     * @return 是否为访问令牌
     */
    boolean isAccessToken(String token);

    /**
     * 检查令牌是否为刷新令牌
     *
     * @param token JWT令牌
     * @return 是否为刷新令牌
     */
    boolean isRefreshToken(String token);

    /**
     * 获取刷新令牌过期时间（秒）
     *
     * @return 过期时间（秒）
     */
    long getRefreshTokenExpirationSeconds();
}
