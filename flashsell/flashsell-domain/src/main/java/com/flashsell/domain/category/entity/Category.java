package com.flashsell.domain.category.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 品类领域实体
 * 代表45个固定品类之一
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Category {

    /**
     * 品类ID
     */
    private Long id;

    /**
     * 所属品类组ID
     */
    private Long groupId;

    /**
     * 品类名称
     */
    private String name;

    /**
     * Amazon品类ID
     */
    private String amazonCategoryId;

    /**
     * 品类下产品数量
     */
    private Integer productCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    // ==================== 领域行为 ====================

    /**
     * 检查品类是否有产品
     *
     * @return 是否有产品
     */
    public boolean hasProducts() {
        return productCount != null && productCount > 0;
    }

    /**
     * 增加产品数量
     *
     * @param count 增加的数量
     */
    public void incrementProductCount(int count) {
        if (this.productCount == null) {
            this.productCount = 0;
        }
        this.productCount += count;
    }

    /**
     * 减少产品数量
     *
     * @param count 减少的数量
     */
    public void decrementProductCount(int count) {
        if (this.productCount == null) {
            this.productCount = 0;
        }
        this.productCount = Math.max(0, this.productCount - count);
    }
}
