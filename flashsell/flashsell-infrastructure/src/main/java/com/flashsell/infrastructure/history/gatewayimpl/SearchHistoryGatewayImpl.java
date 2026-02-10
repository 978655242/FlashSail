package com.flashsell.infrastructure.history.gatewayimpl;

import com.flashsell.domain.history.entity.SearchHistory;
import com.flashsell.domain.history.gateway.SearchHistoryGateway;
import com.flashsell.infrastructure.history.convertor.SearchHistoryConvertor;
import com.flashsell.infrastructure.history.dataobject.SearchHistoryDO;
import com.flashsell.infrastructure.history.mapper.SearchHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 搜索历史网关实现
 * 实现 SearchHistoryGateway 接口，提供搜索历史数据访问的具体实现
 */
@Repository
@RequiredArgsConstructor
public class SearchHistoryGatewayImpl implements SearchHistoryGateway {
    
    private final SearchHistoryMapper searchHistoryMapper;
    private final SearchHistoryConvertor searchHistoryConvertor;
    
    @Override
    public SearchHistory save(SearchHistory searchHistory) {
        if (searchHistory == null) {
            throw new IllegalArgumentException("SearchHistory cannot be null");
        }
        
        SearchHistoryDO searchHistoryDO = searchHistoryConvertor.toDataObject(searchHistory);
        
        if (searchHistoryDO.getCreatedAt() == null) {
            searchHistoryDO.setCreatedAt(LocalDateTime.now());
        }
        
        searchHistoryMapper.insert(searchHistoryDO);
        
        return searchHistoryConvertor.toEntity(searchHistoryDO);
    }
    
    @Override
    public List<SearchHistory> findByUserId(Long userId, int page, int pageSize) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        int offset = page * pageSize;
        List<SearchHistoryDO> searchHistoryDOs = searchHistoryMapper.selectByUserId(userId, offset, pageSize);
        
        return searchHistoryDOs.stream()
                .map(searchHistoryConvertor::toEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SearchHistory> findRecentByUserId(Long userId, int limit) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        List<SearchHistoryDO> searchHistoryDOs = searchHistoryMapper.selectRecentByUserId(userId, limit);
        
        return searchHistoryDOs.stream()
                .map(searchHistoryConvertor::toEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public long countByUserId(Long userId) {
        if (userId == null) {
            return 0;
        }
        
        return searchHistoryMapper.countByUserId(userId);
    }
    
    @Override
    public boolean deleteById(Long id, Long userId) {
        if (id == null || userId == null) {
            throw new IllegalArgumentException("ID and User ID cannot be null");
        }
        
        return searchHistoryMapper.deleteByIdAndUserId(id, userId) > 0;
    }
    
    @Override
    public int deleteByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        return searchHistoryMapper.deleteByUserId(userId);
    }
    
    @Override
    public int deleteBeforeTime(LocalDateTime beforeTime) {
        if (beforeTime == null) {
            throw new IllegalArgumentException("Before time cannot be null");
        }
        
        return searchHistoryMapper.deleteBeforeTime(beforeTime);
    }
}
