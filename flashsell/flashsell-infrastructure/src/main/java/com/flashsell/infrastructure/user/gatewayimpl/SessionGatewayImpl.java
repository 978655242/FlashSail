package com.flashsell.infrastructure.user.gatewayimpl;

import com.flashsell.domain.user.gateway.SessionGateway;
import com.flashsell.infrastructure.common.CacheConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 会话网关实现
 * 使用 Redis 存储和管理用户会话
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SessionGatewayImpl implements SessionGateway {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String TOKEN_FIELD = "token";
    private static final String REFRESH_TOKEN_FIELD = "refreshToken";
    
    @Override
    public void saveSession(Long userId, String token, String refreshToken, long ttlSeconds) {
        String key = buildSessionKey(userId);
        
        redisTemplate.opsForHash().put(key, TOKEN_FIELD, token);
        redisTemplate.opsForHash().put(key, REFRESH_TOKEN_FIELD, refreshToken);
        redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
        
        log.debug("保存用户会话: userId={}, ttl={}s", userId, ttlSeconds);
    }
    
    @Override
    public Optional<String> getToken(Long userId) {
        String key = buildSessionKey(userId);
        Object token = redisTemplate.opsForHash().get(key, TOKEN_FIELD);
        return Optional.ofNullable(token).map(Object::toString);
    }
    
    @Override
    public Optional<String> getRefreshToken(Long userId) {
        String key = buildSessionKey(userId);
        Object refreshToken = redisTemplate.opsForHash().get(key, REFRESH_TOKEN_FIELD);
        return Optional.ofNullable(refreshToken).map(Object::toString);
    }
    
    @Override
    public boolean validateRefreshToken(Long userId, String refreshToken) {
        Optional<String> storedToken = getRefreshToken(userId);
        return storedToken.isPresent() && storedToken.get().equals(refreshToken);
    }
    
    @Override
    public void invalidateSession(Long userId) {
        String key = buildSessionKey(userId);
        Boolean deleted = redisTemplate.delete(key);
        log.debug("使用户会话失效: userId={}, deleted={}", userId, deleted);
    }
    
    @Override
    public boolean hasSession(Long userId) {
        String key = buildSessionKey(userId);
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
    
    @Override
    public void updateSession(Long userId, String newToken, String newRefreshToken, long ttlSeconds) {
        String key = buildSessionKey(userId);
        
        // 检查会话是否存在
        if (!hasSession(userId)) {
            log.warn("尝试更新不存在的会话: userId={}", userId);
            // 如果会话不存在，创建新会话
            saveSession(userId, newToken, newRefreshToken, ttlSeconds);
            return;
        }
        
        redisTemplate.opsForHash().put(key, TOKEN_FIELD, newToken);
        redisTemplate.opsForHash().put(key, REFRESH_TOKEN_FIELD, newRefreshToken);
        redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
        
        log.debug("更新用户会话: userId={}, ttl={}s", userId, ttlSeconds);
    }
    
    /**
     * 构建会话缓存Key
     * 
     * @param userId 用户ID
     * @return 缓存Key
     */
    private String buildSessionKey(Long userId) {
        return CacheConstants.SESSION_PREFIX + userId;
    }
}
