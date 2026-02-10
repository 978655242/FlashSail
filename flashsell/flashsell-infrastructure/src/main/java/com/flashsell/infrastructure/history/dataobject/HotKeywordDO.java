package com.flashsell.infrastructure.history.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 热门关键词数据对象
 * 对应数据库 hot_keywords 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("hot_keywords")
public class HotKeywordDO {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 关键词
     */
    @TableField("keyword")
    private String keyword;
    
    /**
     * 搜索次数
     */
    @TableField("search_count")
    private Integer searchCount;
    
    /**
     * 趋势：UP-上升, DOWN-下降, STABLE-稳定
     */
    @TableField("trend")
    private String trend;
    
    /**
     * 统计日期
     */
    @TableField("stat_date")
    private LocalDate statDate;
    
    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
