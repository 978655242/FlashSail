package com.flashsell.domain.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 1688/Alibaba 商品数据实体
 * 从 Bright Data MCP 爬取并通过 AI 解析的 1688 商品数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlibabaProduct {

    /**
     * 1688 商品 ID
     */
    private String offerId;

    /**
     * 商品标题
     */
    private String title;

    /**
     * 商品图片 URL
     */
    private String imageUrl;

    /**
     * 价格（人民币）
     */
    private BigDecimal price;

    /**
     * 起批价（人民币）
     */
    private BigDecimal minOrderPrice;

    /**
     * 起批量
     */
    private Integer minOrderQuantity;

    /**
     * 供应商名称
     */
    private String supplierName;

    /**
     * 供应商所在地
     */
    private String supplierLocation;

    /**
     * 已售数量
     */
    private Integer soldCount;

    /**
     * 评分
     */
    private Double rating;

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
        return offerId != null && !offerId.isEmpty()
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
     * 检查是否有起批量要求
     */
    public boolean hasMinOrderQuantity() {
        return minOrderQuantity != null && minOrderQuantity > 1;
    }

    /**
     * 将人民币价格转换为美元（使用固定汇率 7.2）
     */
    public BigDecimal getPriceInUsd() {
        if (price == null) {
            return null;
        }
        return price.divide(new BigDecimal("7.2"), 2, java.math.RoundingMode.HALF_UP);
    }
}
