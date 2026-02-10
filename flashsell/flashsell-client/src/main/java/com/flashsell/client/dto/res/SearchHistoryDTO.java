package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 搜索历史 DTO
 * 
 * Requirements: 14.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryDTO {

    /**
     * 搜索历史ID
     */
    private Long id;

    /**
     * 搜索查询内容
     */
    private String query;

    /**
     * 搜索结果数量
     */
    private Integer resultCount;

    /**
     * 搜索时间
     */
    private LocalDateTime createdAt;
}
