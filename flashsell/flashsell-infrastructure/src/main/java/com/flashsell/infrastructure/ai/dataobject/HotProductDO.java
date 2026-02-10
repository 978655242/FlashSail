package com.flashsell.infrastructure.ai.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 爆品推荐数据对象
 * 对应 hot_products 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("hot_products")
public class HotProductDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 品类ID
     */
    private Long categoryId;

    /**
     * 爆品评分（0-100）
     */
    private BigDecimal hotScore;

    /**
     * 品类内排名
     */
    private Integer rankInCategory;

    /**
     * 推荐日期
     */
    private LocalDate recommendDate;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
