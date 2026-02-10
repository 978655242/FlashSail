package com.flashsell.infrastructure.user.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户邀请数据对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_invites")
public class UserInviteDO {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 邀请码
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
}
