package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户使用统计响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUsageStatsRes {

    /**
     * 当月搜索次数
     */
    private Integer searchCount;

    /**
     * 当月导出次数
     */
    private Integer exportCount;

    /**
     * 当前收藏数量
     */
    private Integer favoriteCount;

    /**
     * 当前看板数量
     */
    private Integer boardCount;

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
     * 收藏数量限制
     */
    private Integer favoriteLimit;
}
