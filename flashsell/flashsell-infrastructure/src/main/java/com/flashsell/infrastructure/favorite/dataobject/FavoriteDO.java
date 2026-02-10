package com.flashsell.infrastructure.favorite.dataobject;

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
 * 收藏数据对象
 * 对应数据库 user_favorites 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_favorites")
public class FavoriteDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 产品ID
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 收藏时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
