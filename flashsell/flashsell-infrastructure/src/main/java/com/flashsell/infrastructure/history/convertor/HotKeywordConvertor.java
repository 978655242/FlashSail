package com.flashsell.infrastructure.history.convertor;

import com.flashsell.domain.history.entity.HotKeyword;
import com.flashsell.infrastructure.history.dataobject.HotKeywordDO;
import org.springframework.stereotype.Component;

/**
 * 热门关键词转换器
 * 负责 HotKeywordDO 和 HotKeyword 领域实体之间的转换
 */
@Component
public class HotKeywordConvertor {
    
    /**
     * 将数据对象转换为领域实体
     * 
     * @param hotKeywordDO 热门关键词数据对象
     * @return 热门关键词领域实体
     */
    public HotKeyword toEntity(HotKeywordDO hotKeywordDO) {
        if (hotKeywordDO == null) {
            return null;
        }
        
        return HotKeyword.builder()
                .id(hotKeywordDO.getId())
                .keyword(hotKeywordDO.getKeyword())
                .searchCount(hotKeywordDO.getSearchCount())
                .trend(hotKeywordDO.getTrend())
                .statDate(hotKeywordDO.getStatDate())
                .createdAt(hotKeywordDO.getCreatedAt())
                .build();
    }
    
    /**
     * 将领域实体转换为数据对象
     * 
     * @param hotKeyword 热门关键词领域实体
     * @return 热门关键词数据对象
     */
    public HotKeywordDO toDataObject(HotKeyword hotKeyword) {
        if (hotKeyword == null) {
            return null;
        }
        
        return HotKeywordDO.builder()
                .id(hotKeyword.getId())
                .keyword(hotKeyword.getKeyword())
                .searchCount(hotKeyword.getSearchCount())
                .trend(hotKeyword.getTrend())
                .statDate(hotKeyword.getStatDate())
                .createdAt(hotKeyword.getCreatedAt())
                .build();
    }
}
