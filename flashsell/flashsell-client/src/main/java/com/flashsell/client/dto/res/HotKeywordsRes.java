package com.flashsell.client.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 热门关键词响应 DTO
 * 
 * Requirements: 13.5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotKeywordsRes {

    /**
     * 热门关键词列表
     */
    private List<HotKeywordDTO> keywords;
}
