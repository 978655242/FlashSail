package com.flashsell.infrastructure.history.dataobject;

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
 * 浏览历史数据对象
 * 对应数据库 browse_history 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("browse_history")
public class BrowseHistoryDO {
    
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
     * 浏览时间（重复浏览更新此时间）
     */
    @TableField("browsed_at")
    private LocalDateTime browsedAt;
}
