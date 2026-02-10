package com.flashsell.infrastructure.history.gatewayimpl;

import com.flashsell.domain.history.entity.BrowseHistory;
import com.flashsell.domain.history.gateway.BrowseHistoryGateway;
import com.flashsell.infrastructure.history.convertor.BrowseHistoryConvertor;
import com.flashsell.infrastructure.history.dataobject.BrowseHistoryDO;
import com.flashsell.infrastructure.history.mapper.BrowseHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 浏览历史网关实现
 * 实现 BrowseHistoryGateway 接口，提供浏览历史数据访问的具体实现
 */
@Repository
@RequiredArgsConstructor
public class BrowseHistoryGatewayImpl implements BrowseHistoryGateway {
    
    private final BrowseHistoryMapper browseHistoryMapper;
    private final BrowseHistoryConvertor browseHistoryConvertor;
    
    @Override
    public BrowseHistory saveOrUpdate(BrowseHistory browseHistory) {
        if (browseHistory == null) {
            throw new IllegalArgumentException("BrowseHistory cannot be null");
        }
        
        // 检查是否已存在
        BrowseHistoryDO existingDO = browseHistoryMapper.selectByUserIdAndProductId(
                browseHistory.getUserId(), 
                browseHistory.getProductId()
        );
        
        if (existingDO != null) {
            // 更新浏览时间
            LocalDateTime browsedAt = browseHistory.getBrowsedAt() != null 
                    ? browseHistory.getBrowsedAt() 
                    : LocalDateTime.now();
            browseHistoryMapper.updateBrowsedAt(
                    browseHistory.getUserId(), 
                    browseHistory.getProductId(), 
                    browsedAt
            );
            existingDO.setBrowsedAt(browsedAt);
            return browseHistoryConvertor.toEntity(existingDO);
        } else {
            // 新增浏览历史
            BrowseHistoryDO browseHistoryDO = browseHistoryConvertor.toDataObject(browseHistory);
            if (browseHistoryDO.getBrowsedAt() == null) {
                browseHistoryDO.setBrowsedAt(LocalDateTime.now());
            }
            browseHistoryMapper.insert(browseHistoryDO);
            return browseHistoryConvertor.toEntity(browseHistoryDO);
        }
    }
    
    @Override
    public BrowseHistory findByUserIdAndProductId(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return null;
        }
        
        BrowseHistoryDO browseHistoryDO = browseHistoryMapper.selectByUserIdAndProductId(userId, productId);
        return browseHistoryConvertor.toEntity(browseHistoryDO);
    }
    
    @Override
    public List<BrowseHistory> findByUserId(Long userId, int page, int pageSize) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        int offset = page * pageSize;
        List<BrowseHistoryDO> browseHistoryDOs = browseHistoryMapper.selectByUserId(userId, offset, pageSize);
        
        return browseHistoryDOs.stream()
                .map(browseHistoryConvertor::toEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<BrowseHistory> findRecentByUserId(Long userId, int limit) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        List<BrowseHistoryDO> browseHistoryDOs = browseHistoryMapper.selectRecentByUserId(userId, limit);
        
        return browseHistoryDOs.stream()
                .map(browseHistoryConvertor::toEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public long countByUserId(Long userId) {
        if (userId == null) {
            return 0;
        }
        
        return browseHistoryMapper.countByUserId(userId);
    }
    
    @Override
    public boolean deleteByUserIdAndProductId(Long userId, Long productId) {
        if (userId == null || productId == null) {
            throw new IllegalArgumentException("User ID and Product ID cannot be null");
        }
        
        return browseHistoryMapper.deleteByUserIdAndProductId(userId, productId) > 0;
    }
    
    @Override
    public int deleteByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        return browseHistoryMapper.deleteByUserId(userId);
    }
    
    @Override
    public int deleteBeforeTime(LocalDateTime beforeTime) {
        if (beforeTime == null) {
            throw new IllegalArgumentException("Before time cannot be null");
        }
        
        return browseHistoryMapper.deleteBeforeTime(beforeTime);
    }
}
