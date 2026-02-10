package com.flashsell.adapter.web;

import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 * 从 SecurityContext 中获取当前用户信息
 *
 * JwtAuthenticationFilter 中设置的 principal 是 Long 类型的 userId
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户的 ID
     *
     * @return 用户 ID
     * @throws IllegalStateException 如果用户未登录
     */
    public static Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal == null) {
            throw new IllegalStateException("用户未登录");
        }

        // JwtAuthenticationFilter 设置的 principal 是 Long 类型的 userId
        if (principal instanceof Long) {
            return (Long) principal;
        }

        // 如果是 String 类型，尝试转换
        if (principal instanceof String) {
            try {
                return Long.valueOf((String) principal);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("无效的用户ID: " + principal);
            }
        }

        throw new IllegalStateException("不支持的用户认证类型: " + principal.getClass());
    }

    /**
     * 检查用户是否已登录
     *
     * @return 是否已登录
     */
    public static boolean isAuthenticated() {
        return SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
    }
}
