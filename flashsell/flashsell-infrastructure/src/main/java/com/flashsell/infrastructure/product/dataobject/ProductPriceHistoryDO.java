package com.flashsell.infrastructure.product.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
 * 产品价格历史数据对象
 * 对应数据库 product_price_history 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("product_price_history")
public class ProductPriceHistoryDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 产品ID
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 价格（美元）
     */
    private BigDecimal price;

    /**
     * 记录日期
     */
    @TableField("recorded_date")
    private LocalDate recordedDate;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
