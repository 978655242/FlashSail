package com.flashsell.infrastructure.history.convertor;

import com.flashsell.domain.history.entity.BrowseHistory;
import com.flashsell.infrastructure.history.dataobject.BrowseHistoryDO;
import org.springframework.stereotype.Component;

/**
 * 浏览历史转换器
 * 负责 BrowseHistoryDO 和 BrowseHistory 领域实体之间的转换
 */
@Component
public class BrowseHistoryConvertor {
    
    /**
     * 将数据对象转换为领域实体
     * 
     * @param browseHistoryDO 浏览历史数据对象
     * @return 浏览历史领域实体
     */
    public BrowseHistory toEntity(BrowseHistoryDO browseHistoryDO) {
        if (browseHistoryDO == null) {
            return null;
        }
        
        return BrowseHistory.builder()
                .id(browseHistoryDO.getId())
                .userId(browseHistoryDO.getUserId())
                .productId(browseHistoryDO.getProductId())
                .browsedAt(browseHistoryDO.getBrowsedAt())
                .build();
    }
    
    /**
     * 将领域实体转换为数据对象
     * 
     * @param browseHistory 浏览历史领域实体
     * @return 浏览历史数据对象
     */
    public BrowseHistoryDO toDataObject(BrowseHistory browseHistory) {
        if (browseHistory == null) {
            return null;
        }
        
        return BrowseHistoryDO.builder()
                .id(browseHistory.getId())
                .userId(browseHistory.getUserId())
                .productId(browseHistory.getProductId())
                .browsedAt(browseHistory.getBrowsedAt())
                .build();
    }
}
