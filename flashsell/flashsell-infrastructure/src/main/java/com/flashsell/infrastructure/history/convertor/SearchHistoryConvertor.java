package com.flashsell.infrastructure.history.convertor;

import com.flashsell.domain.history.entity.SearchHistory;
import com.flashsell.infrastructure.history.dataobject.SearchHistoryDO;
import org.springframework.stereotype.Component;

/**
 * 搜索历史转换器
 * 负责 SearchHistoryDO 和 SearchHistory 领域实体之间的转换
 */
@Component
public class SearchHistoryConvertor {
    
    /**
     * 将数据对象转换为领域实体
     * 
     * @param searchHistoryDO 搜索历史数据对象
     * @return 搜索历史领域实体
     */
    public SearchHistory toEntity(SearchHistoryDO searchHistoryDO) {
        if (searchHistoryDO == null) {
            return null;
        }
        
        return SearchHistory.builder()
                .id(searchHistoryDO.getId())
                .userId(searchHistoryDO.getUserId())
                .query(searchHistoryDO.getQuery())
                .resultCount(searchHistoryDO.getResultCount())
                .createdAt(searchHistoryDO.getCreatedAt())
                .build();
    }
    
    /**
     * 将领域实体转换为数据对象
     * 
     * @param searchHistory 搜索历史领域实体
     * @return 搜索历史数据对象
     */
    public SearchHistoryDO toDataObject(SearchHistory searchHistory) {
        if (searchHistory == null) {
            return null;
        }
        
        return SearchHistoryDO.builder()
                .id(searchHistory.getId())
                .userId(searchHistory.getUserId())
                .query(searchHistory.getQuery())
                .resultCount(searchHistory.getResultCount())
                .createdAt(searchHistory.getCreatedAt())
                .build();
    }
}
