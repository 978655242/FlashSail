package com.flashsell.domain.user.gateway;

import com.flashsell.domain.user.entity.User;

import java.util.Optional;

/**
 * 用户网关接口
 * 定义用户数据访问的抽象接口，由 infrastructure 层实现
 */
public interface UserGateway {
    
    /**
     * 根据ID查询用户
     * 
     * @param id 用户ID
     * @return 用户实体（可能为空）
     */
    Optional<User> findById(Long id);
    
    /**
     * 根据手机号查询用户
     * 
     * @param phone 手机号
     * @return 用户实体（可能为空）
     */
    Optional<User> findByPhone(String phone);
    
    /**
     * 根据邮箱查询用户
     * 
     * @param email 邮箱
     * @return 用户实体（可能为空）
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 保存用户（新增或更新）
     * 
     * @param user 用户实体
     * @return 保存后的用户实体（包含生成的ID）
     */
    User save(User user);
    
    /**
     * 更新用户信息
     * 
     * @param user 用户实体
     */
    void update(User user);
    
    /**
     * 检查手机号是否已存在
     * 
     * @param phone 手机号
     * @return 是否存在
     */
    boolean existsByPhone(String phone);
    
    /**
     * 检查邮箱是否已存在
     * 
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 软删除用户
     *
     * @param userId 用户ID
     */
    void softDelete(Long userId);

    /**
     * 更新用户订阅级别
     *
     * @param userId       用户ID
     * @param level        订阅级别
     * @param expireDate   过期时间
     */
    void updateSubscriptionLevel(Long userId, com.flashsell.domain.user.entity.SubscriptionLevel level, java.time.LocalDateTime expireDate);
}
