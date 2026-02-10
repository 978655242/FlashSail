package com.flashsell.infrastructure.user.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户使用统计数据对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_usage_stats")
public class UserUsageStatsDO {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 统计月份（格式：YYYY-MM）
     */
    private String month;
    
    /**
     * 搜索次数
     */
    private Integer searchCount;
    
    /**
     * 导出次数
     */
    private Integer exportCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
