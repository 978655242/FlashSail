package com.flashsell.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户邀请实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInvite {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 邀请码（唯一）
     */
    private String inviteCode;
    
    /**
     * 已邀请人数
     */
    private Integer invitedCount;
    
    /**
     * 获得的奖励天数
     */
    private Integer rewardDays;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 增加邀请计数
     * 
     * @param rewardDays 每次邀请奖励的天数
     */
    public void incrementInviteCount(int rewardDays) {
        this.invitedCount = (this.invitedCount == null ? 0 : this.invitedCount) + 1;
        this.rewardDays = (this.rewardDays == null ? 0 : this.rewardDays) + rewardDays;
    }
}
