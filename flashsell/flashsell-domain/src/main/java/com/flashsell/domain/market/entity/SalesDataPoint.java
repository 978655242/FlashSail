package com.flashsell.domain.market.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 销量数据点实体
 * 用于市场分析中的销量分布数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesDataPoint {

    /**
     * 日期
     */
    private LocalDate date;

    /**
     * 销量（估算值）
     */
    private Long salesVolume;

    /**
     * 平均价格
     */
    private BigDecimal averagePrice;

    /**
     * 产品数量
     */
    private Integer productCount;

    /**
     * 环比增长率（百分比）
     */
    private BigDecimal growthRate;

    // ==================== 领域行为 ====================

    /**
     * 检查是否为有效数据点
     *
     * @return 是否有效
     */
    public boolean isValid() {
        return date != null && salesVolume != null && salesVolume >= 0;
    }

    /**
     * 检查是否为增长数据点（环比增长率>0）
     *
     * @return 是否增长
     */
    public boolean isGrowing() {
        return growthRate != null && growthRate.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 检查是否为下降数据点（环比增长率<0）
     *
     * @return 是否下降
     */
    public boolean isDeclining() {
        return growthRate != null && growthRate.compareTo(BigDecimal.ZERO) < 0;
    }
}
