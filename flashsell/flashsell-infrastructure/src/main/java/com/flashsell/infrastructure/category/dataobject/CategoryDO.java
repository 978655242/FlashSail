package com.flashsell.infrastructure.category.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 品类数据对象
 * 对应数据库 categories 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("categories")
public class CategoryDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属类目组ID
     */
    @TableField("group_id")
    private Long groupId;

    /**
     * 品类名称
     */
    private String name;

    /**
     * Amazon品类ID
     */
    @TableField("amazon_category_id")
    private String amazonCategoryId;

    /**
     * 品类下产品数量
     */
    @TableField("product_count")
    private Integer productCount;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
