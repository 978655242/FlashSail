package com.flashsell.domain.product.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 价格点领域实体
 * 代表产品在某一天的价格记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricePoint {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 价格（美元）
     */
    private BigDecimal price;

    /**
     * 记录日期
     */
    private LocalDate recordedDate;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    // ==================== 领域行为 ====================

    /**
     * 检查价格是否有效
     *
     * @return 是否有效
     */
    public boolean isValidPrice() {
        return price != null && price.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 计算与另一个价格点的价格变化百分比
     *
     * @param other 另一个价格点
     * @return 价格变化百分比（正数表示上涨，负数表示下跌）
     */
    public BigDecimal calculatePriceChangePercent(PricePoint other) {
        if (other == null || other.getPrice() == null || other.getPrice().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return this.price.subtract(other.getPrice())
                .divide(other.getPrice(), 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }
}
