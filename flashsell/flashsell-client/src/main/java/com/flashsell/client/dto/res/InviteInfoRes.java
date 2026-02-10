package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邀请信息响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteInfoRes {

    /**
     * 邀请码
     */
    private String inviteCode;

    /**
     * 邀请链接
     */
    private String inviteLink;

    /**
     * 邀请链接（别名，兼容前端）
     */
    public String getInviteUrl() {
        return inviteLink;
    }

    public void setInviteUrl(String inviteUrl) {
        this.inviteLink = inviteUrl;
    }

    /**
     * 已邀请人数
     */
    private Integer invitedCount;

    /**
     * 获得的奖励天数
     */
    private Integer rewardDays;
}
