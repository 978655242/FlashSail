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
 * 搜索历史数据对象
 * 对应数据库 search_history 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("search_history")
public class SearchHistoryDO {
    
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
     * 搜索查询内容
     */
    @TableField("query")
    private String query;
    
    /**
     * 搜索结果数量
     */
    @TableField("result_count")
    private Integer resultCount;
    
    /**
     * 搜索时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
