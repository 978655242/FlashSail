package com.flashsell.infrastructure.user.convertor;

import com.flashsell.domain.user.entity.UserInvite;
import com.flashsell.infrastructure.user.dataobject.UserInviteDO;
import org.springframework.stereotype.Component;

/**
 * 用户邀请转换器
 */
@Component
public class UserInviteConvertor {
    
    /**
     * DO转Entity
     * 
     * @param inviteDO 数据对象
     * @return 领域实体
     */
    public UserInvite toEntity(UserInviteDO inviteDO) {
        if (inviteDO == null) {
            return null;
        }
        
        return UserInvite.builder()
                .id(inviteDO.getId())
                .userId(inviteDO.getUserId())
                .inviteCode(inviteDO.getInviteCode())
                .invitedCount(inviteDO.getInvitedCount())
                .rewardDays(inviteDO.getRewardDays())
                .createdAt(inviteDO.getCreatedAt())
                .build();
    }
    
    /**
     * Entity转DO
     * 
     * @param invite 领域实体
     * @return 数据对象
     */
    public UserInviteDO toDO(UserInvite invite) {
        if (invite == null) {
            return null;
        }
        
        return UserInviteDO.builder()
                .id(invite.getId())
                .userId(invite.getUserId())
                .inviteCode(invite.getInviteCode())
                .invitedCount(invite.getInvitedCount())
                .rewardDays(invite.getRewardDays())
                .createdAt(invite.getCreatedAt())
                .build();
    }
}
