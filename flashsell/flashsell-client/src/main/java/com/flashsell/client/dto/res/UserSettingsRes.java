package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户设置响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsRes {

    /**
     * 是否开启消息通知
     */
    private Boolean notificationEnabled;

    /**
     * 是否订阅邮件
     */
    private Boolean emailSubscribed;

    /**
     * 是否开启两步验证
     */
    private Boolean twoFactorEnabled;

    /**
     * 手机号是否已验证
     */
    private Boolean phoneVerified;
}
