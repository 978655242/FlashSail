package com.flashsell.domain.board.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 看板领域实体
 * 表示用户创建的产品看板，用于组织和管理收藏的产品
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Board {

    /**
     * 看板ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 看板名称
     */
    private String name;

    /**
     * 看板中的产品ID列表
     */
    @Builder.Default
    private List<Long> productIds = new ArrayList<>();

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 删除时间（软删除）
     */
    private LocalDateTime deletedAt;

    // ==================== 领域行为 ====================

    /**
     * 检查看板是否属于指定用户
     *
     * @param userId 用户ID
     * @return 是否属于该用户
     */
    public boolean belongsToUser(Long userId) {
        return this.userId != null && this.userId.equals(userId);
    }

    /**
     * 检查看板是否有效（用户ID和名称都不为空）
     *
     * @return 是否有效
     */
    public boolean isValid() {
        return userId != null && name != null && !name.trim().isEmpty();
    }

    /**
     * 检查看板是否已被删除（软删除）
     *
     * @return 是否已删除
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * 软删除看板
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 获取看板中的产品数量
     *
     * @return 产品数量
     */
    public int getProductCount() {
        return productIds != null ? productIds.size() : 0;
    }

    /**
     * 检查产品是否已在看板中
     *
     * @param productId 产品ID
     * @return 是否已在看板中
     */
    public boolean containsProduct(Long productId) {
        return productIds != null && productIds.contains(productId);
    }

    /**
     * 添加产品到看板
     *
     * @param productId 产品ID
     * @return 是否添加成功（如果已存在则返回false）
     */
    public boolean addProduct(Long productId) {
        if (productId == null) {
            return false;
        }
        if (productIds == null) {
            productIds = new ArrayList<>();
        }
        if (productIds.contains(productId)) {
            return false;
        }
        productIds.add(productId);
        return true;
    }

    /**
     * 从看板移除产品
     *
     * @param productId 产品ID
     * @return 是否移除成功
     */
    public boolean removeProduct(Long productId) {
        if (productId == null || productIds == null) {
            return false;
        }
        return productIds.remove(productId);
    }

    /**
     * 批量添加产品到看板
     *
     * @param productIds 产品ID列表
     * @return 成功添加的产品数量
     */
    public int addProducts(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return 0;
        }
        int addedCount = 0;
        for (Long productId : productIds) {
            if (addProduct(productId)) {
                addedCount++;
            }
        }
        return addedCount;
    }

    /**
     * 创建新的看板实例
     *
     * @param userId 用户ID
     * @param name 看板名称
     * @return 看板实体
     */
    public static Board create(Long userId, String name) {
        return Board.builder()
                .userId(userId)
                .name(name)
                .productIds(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
