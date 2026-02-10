package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户资料响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRes {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 订阅等级
     */
    private String subscriptionLevel;

    /**
     * 订阅到期日期
     */
    private LocalDate subscriptionExpireDate;

    /**
     * 手机号是否已验证
     */
    private Boolean phoneVerified;

    /**
     * 是否开启两步验证
     */
    private Boolean twoFactorEnabled;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 注册时间
     */
    private LocalDateTime createdAt;
}
