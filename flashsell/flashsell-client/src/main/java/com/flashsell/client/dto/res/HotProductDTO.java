package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 爆品DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotProductDTO {

    /**
     * 产品信息
     */
    private ProductItemRes product;

    /**
     * 爆品评分（0-100）
     */
    private BigDecimal hotScore;

    /**
     * 品类内排名
     */
    private Integer rankInCategory;

    /**
     * 上榜天数
     */
    private Long daysOnList;

    /**
     * 排名变化（正数上升，负数下降，0不变）
     */
    private Integer rankChange;

    /**
     * 推荐理由
     */
    private String recommendation;

    /**
     * 推荐日期
     */
    private LocalDate recommendDate;
}
