package com.flashsell.infrastructure.payment.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订阅套餐数据对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("subscription_plans")
public class SubscriptionPlanDO {

    /**
     * 套餐ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 套餐名称
     */
    private String name;

    /**
     * 套餐描述
     */
    private String description;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 订阅时长（天）
     */
    private Integer durationDays;

    /**
     * 搜索次数限制
     */
    private Integer searchLimit;

    /**
     * 导出次数限制
     */
    private Integer exportLimit;

    /**
     * 看板数量限制
     */
    private Integer boardLimit;

    /**
     * 是否支持AI分析
     */
    private Boolean aiAnalysisEnabled;

    /**
     * 是否支持API访问
     */
    private Boolean apiAccessEnabled;

    /**
     * 套餐级别
     */
    private String level;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
