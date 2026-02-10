package com.flashsell.domain.user.gateway;

import com.flashsell.domain.user.entity.UserInvite;

import java.util.Optional;

/**
 * 用户邀请网关接口
 */
public interface UserInviteGateway {
    
    /**
     * 根据用户ID查询邀请信息
     * 
     * @param userId 用户ID
     * @return 邀请信息（可能为空）
     */
    Optional<UserInvite> findByUserId(Long userId);
    
    /**
     * 根据邀请码查询邀请信息
     * 
     * @param inviteCode 邀请码
     * @return 邀请信息（可能为空）
     */
    Optional<UserInvite> findByInviteCode(String inviteCode);
    
    /**
     * 保存邀请信息
     * 
     * @param userInvite 邀请信息
     * @return 保存后的邀请信息
     */
    UserInvite save(UserInvite userInvite);
    
    /**
     * 更新邀请信息
     * 
     * @param userInvite 邀请信息
     */
    void update(UserInvite userInvite);
    
    /**
     * 检查邀请码是否已存在
     * 
     * @param inviteCode 邀请码
     * @return 是否存在
     */
    boolean existsByInviteCode(String inviteCode);
}
