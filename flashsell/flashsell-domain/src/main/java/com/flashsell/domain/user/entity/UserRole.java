package com.flashsell.domain.user.entity;

import lombok.Getter;

/**
 * 用户角色枚举
 * 定义用户的角色类型，用于权限控制
 */
@Getter
public enum UserRole {

    /**
     * 普通用户
     */
    USER("用户", "ROLE_USER"),

    /**
     * 管理员
     */
    ADMIN("管理员", "ROLE_ADMIN");

    /**
     * 角色显示名称
     */
    private final String displayName;

    /**
     * Spring Security 权限标识
     */
    private final String authority;

    UserRole(String displayName, String authority) {
        this.displayName = displayName;
        this.authority = authority;
    }

    /**
     * 根据字符串解析角色
     *
     * @param role 角色字符串
     * @return 角色枚举，默认返回 USER
     */
    public static UserRole fromString(String role) {
        if (role == null || role.isEmpty()) {
            return USER;
        }
        try {
            return valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return USER;
        }
    }

    /**
     * 检查是否为管理员
     *
     * @return 是否为管理员
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }
}
