package com.flashsell.domain.favorite.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 收藏领域实体
 * 表示用户对产品的收藏关系
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Favorite {

    /**
     * 收藏ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 收藏时间
     */
    private LocalDateTime createdAt;

    // ==================== 领域行为 ====================

    /**
     * 检查收藏是否属于指定用户
     *
     * @param userId 用户ID
     * @return 是否属于该用户
     */
    public boolean belongsToUser(Long userId) {
        return this.userId != null && this.userId.equals(userId);
    }

    /**
     * 检查收藏是否有效（用户ID和产品ID都不为空）
     *
     * @return 是否有效
     */
    public boolean isValid() {
        return userId != null && productId != null;
    }

    /**
     * 创建新的收藏实例
     *
     * @param userId 用户ID
     * @param productId 产品ID
     * @return 收藏实体
     */
    public static Favorite create(Long userId, Long productId) {
        return Favorite.builder()
                .userId(userId)
                .productId(productId)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
