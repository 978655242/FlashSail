package com.flashsell.domain.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Amazon 商品数据实体
 * 从 Bright Data MCP 获取的 Amazon 商品原始数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmazonProduct {

    /**
     * Amazon 标准识别号
     */
    private String asin;

    /**
     * 商品标题
     */
    private String title;

    /**
     * 商品图片 URL
     */
    private String imageUrl;

    /**
     * 当前价格（美元）
     */
    private BigDecimal price;

    /**
     * 原价（美元）
     */
    private BigDecimal originalPrice;

    /**
     * 评分（1.0-5.0）
     */
    private Double rating;

    /**
     * 评论数
     */
    private Integer reviewCount;

    /**
     * BSR 排名（Best Sellers Rank）
     */
    private Integer bsrRank;

    /**
     * 品类
     */
    private String category;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 店铺名称
     */
    private String store;

    /**
     * 商品特性列表
     */
    private List<String> features;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品 URL
     */
    private String productUrl;

    /**
     * 数据获取时间
     */
    private LocalDateTime fetchedAt;

    // ==================== 领域行为 ====================

    /**
     * 检查数据是否有效（包含必需字段）
     */
    public boolean isValid() {
        return asin != null && !asin.isEmpty()
                && title != null && !title.isEmpty()
                && price != null;
    }

    /**
     * 检查数据是否过期（超过1小时）
     */
    public boolean isStale() {
        if (fetchedAt == null) {
            return true;
        }
        return fetchedAt.isBefore(LocalDateTime.now().minusHours(1));
    }

    /**
     * 检查是否有折扣
     */
    public boolean hasDiscount() {
        return originalPrice != null && price != null
                && originalPrice.compareTo(price) > 0;
    }

    /**
     * 计算折扣百分比
     */
    public BigDecimal getDiscountPercentage() {
        if (!hasDiscount()) {
            return BigDecimal.ZERO;
        }
        return originalPrice.subtract(price)
                .divide(originalPrice, 2, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }
}
