package com.flashsell.infrastructure.user.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户数据对象
 * 对应数据库 users 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class UserDO {

    /**
     * 用户ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 手机号，唯一
     */
    private String phone;

    /**
     * 密码哈希值（BCrypt加密）
     */
    @TableField("password_hash")
    private String passwordHash;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 订阅等级：FREE-免费版, BASIC-基础版, PRO-专业版
     */
    @TableField("subscription_level")
    private String subscriptionLevel;

    /**
     * 订阅到期日期
     */
    @TableField("subscription_expire_date")
    private LocalDate subscriptionExpireDate;

    /**
     * 是否开启消息通知
     */
    @TableField("notification_enabled")
    private Boolean notificationEnabled;

    /**
     * 是否订阅邮件
     */
    @TableField("email_subscribed")
    private Boolean emailSubscribed;

    /**
     * 是否开启两步验证
     */
    @TableField("two_factor_enabled")
    private Boolean twoFactorEnabled;

    /**
     * TOTP两步验证密钥
     */
    @TableField("two_factor_secret")
    private String twoFactorSecret;

    /**
     * 手机号是否已验证
     */
    @TableField("phone_verified")
    private Boolean phoneVerified;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 删除时间（软删除）
     */
    @TableLogic(value = "null", delval = "now()")
    @TableField("deleted_at")
    private LocalDateTime deletedAt;
}
