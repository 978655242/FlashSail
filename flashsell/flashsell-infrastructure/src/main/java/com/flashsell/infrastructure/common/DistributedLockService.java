package com.flashsell.infrastructure.common;

import com.flashsell.domain.common.gateway.DistributedLockGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁服务实现
 * 基于 Redis 实现分布式锁，防止多实例部署时任务重复执行
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockService implements DistributedLockGateway {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String LOCK_PREFIX = "flashsell:lock:";
    private static final long DEFAULT_WAIT_TIME = 0;
    private static final long DEFAULT_LEASE_TIME = 300; // 5 minutes

    // Lua script for atomic lock release
    private static final String RELEASE_LOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

    @Override
    public String tryLock(String lockKey) {
        return tryLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME);
    }

    @Override
    public String tryLock(String lockKey, long waitTime, long leaseTime) {
        String lockKeyFull = LOCK_PREFIX + lockKey;
        String lockValue = UUID.randomUUID().toString();

        try {
            // 尝试设置锁，使用 SET NX EX 原子操作
            Boolean success = redisTemplate.opsForValue()
                    .setIfAbsent(lockKeyFull, lockValue, leaseTime, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(success)) {
                log.debug("成功获取分布式锁: key={}, value={}", lockKeyFull, lockValue);
                return lockValue;
            }

            log.debug("获取分布式锁失败: key={}", lockKeyFull);
            return null;
        } catch (Exception e) {
            log.error("获取分布式锁异常: key={}", lockKeyFull, e);
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean releaseLock(String lockKey, String lockValue) {
        if (lockValue == null) {
            return false;
        }

        String lockKeyFull = LOCK_PREFIX + lockKey;

        try {
            // 使用 Lua 脚本保证原子性
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(RELEASE_LOCK_SCRIPT);
            redisScript.setResultType(Long.class);

            Long result = redisTemplate.execute(
                    redisScript,
                    Collections.singletonList(lockKeyFull),
                    lockValue
            );

            boolean released = Long.valueOf(1).equals(result);
            if (released) {
                log.debug("成功释放分布式锁: key={}", lockKeyFull);
            } else {
                log.warn("释放分布式锁失败（锁不存在或已被其他进程持有）: key={}", lockKeyFull);
            }
            return released;
        } catch (Exception e) {
            log.error("释放分布式锁异常: key={}", lockKeyFull, e);
            return false;
        }
    }

    @Override
    public boolean isLocked(String lockKey) {
        String lockKeyFull = LOCK_PREFIX + lockKey;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKeyFull));
    }

    @Override
    public boolean executeWithLock(String lockKey, Runnable task) {
        String lockValue = tryLock(lockKey);
        if (lockValue == null) {
            log.info("无法获取锁，跳过任务执行: lockKey={}", lockKey);
            return false;
        }

        try {
            task.run();
            return true;
        } finally {
            releaseLock(lockKey, lockValue);
        }
    }
}
