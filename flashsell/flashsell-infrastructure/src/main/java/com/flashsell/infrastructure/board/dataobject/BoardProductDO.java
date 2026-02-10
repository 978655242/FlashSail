package com.flashsell.infrastructure.board.dataobject;

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
 * 看板产品关联数据对象
 * 对应数据库 board_products 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("board_products")
public class BoardProductDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 看板ID
     */
    @TableField("board_id")
    private Long boardId;

    /**
     * 产品ID
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 添加时间
     */
    @TableField("added_at")
    private LocalDateTime addedAt;
}
