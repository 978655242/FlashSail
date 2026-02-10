package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 搜索历史响应 DTO
 * 
 * Requirements: 14.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryRes {
    
    /**
     * 搜索历史列表
     */
    private List<SearchHistoryDTO> histories;
    
    /**
     * 总数
     */
    private Long total;
    
    /**
     * 当前页码
     */
    private Integer page;
}
