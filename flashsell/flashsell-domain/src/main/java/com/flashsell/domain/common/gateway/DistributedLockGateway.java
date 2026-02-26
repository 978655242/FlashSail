package com.flashsell.domain.common.gateway;

/**
 * 分布式锁网关接口
 * 定义分布式锁的抽象接口，由 infrastructure 层实现
 */
public interface DistributedLockGateway {

    /**
     * 尝试获取分布式锁
     *
     * @param lockKey 锁的键
     * @return 锁的值（用于释放锁），如果获取失败返回 null
     */
    String tryLock(String lockKey);

    /**
     * 尝试获取分布式锁
     *
     * @param lockKey 锁的键
     * @param waitTime 等待时间（秒）
     * @param leaseTime 锁的持有时间（秒）
     * @return 锁的值（用于释放锁），如果获取失败返回 null
     */
    String tryLock(String lockKey, long waitTime, long leaseTime);

    /**
     * 释放分布式锁
     *
     * @param lockKey 锁的键
     * @param lockValue 锁的值
     * @return 是否释放成功
     */
    boolean releaseLock(String lockKey, String lockValue);

    /**
     * 检查锁是否存在
     *
     * @param lockKey 锁的键
     * @return 锁是否存在
     */
    boolean isLocked(String lockKey);

    /**
     * 执行带有分布式锁的任务
     *
     * @param lockKey 锁的键
     * @param task 要执行的任务
     * @return 任务是否执行成功（获取锁失败返回 false）
     */
    boolean executeWithLock(String lockKey, Runnable task);
}
