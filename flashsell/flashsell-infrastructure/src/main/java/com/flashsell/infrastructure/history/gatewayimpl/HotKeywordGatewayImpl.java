package com.flashsell.infrastructure.history.gatewayimpl;

import com.flashsell.domain.history.entity.HotKeyword;
import com.flashsell.domain.history.gateway.HotKeywordGateway;
import com.flashsell.infrastructure.history.convertor.HotKeywordConvertor;
import com.flashsell.infrastructure.history.dataobject.HotKeywordDO;
import com.flashsell.infrastructure.history.mapper.HotKeywordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 热门关键词网关实现
 * 实现 HotKeywordGateway 接口，提供热门关键词数据访问的具体实现
 */
@Repository
@RequiredArgsConstructor
public class HotKeywordGatewayImpl implements HotKeywordGateway {
    
    private final HotKeywordMapper hotKeywordMapper;
    private final HotKeywordConvertor hotKeywordConvertor;
    
    @Override
    public HotKeyword saveOrUpdate(HotKeyword hotKeyword) {
        if (hotKeyword == null) {
            throw new IllegalArgumentException("HotKeyword cannot be null");
        }
        
        HotKeywordDO hotKeywordDO = hotKeywordConvertor.toDataObject(hotKeyword);
        
        // 检查是否已存在
        HotKeywordDO existingDO = hotKeywordMapper.selectByKeywordAndDate(
                hotKeyword.getKeyword(), 
                hotKeyword.getStatDate()
        );
        
        if (existingDO != null) {
            // 更新
            hotKeywordDO.setId(existingDO.getId());
            hotKeywordDO.setCreatedAt(existingDO.getCreatedAt());
            hotKeywordMapper.updateById(hotKeywordDO);
        } else {
            // 新增
            if (hotKeywordDO.getCreatedAt() == null) {
                hotKeywordDO.setCreatedAt(LocalDateTime.now());
            }
            hotKeywordMapper.insert(hotKeywordDO);
        }
        
        return hotKeywordConvertor.toEntity(hotKeywordDO);
    }
    
    @Override
    public HotKeyword findByKeywordAndDate(String keyword, LocalDate statDate) {
        if (keyword == null || statDate == null) {
            return null;
        }
        
        HotKeywordDO hotKeywordDO = hotKeywordMapper.selectByKeywordAndDate(keyword, statDate);
        return hotKeywordConvertor.toEntity(hotKeywordDO);
    }
    
    @Override
    public List<HotKeyword> findTopByDate(LocalDate statDate, int limit) {
        if (statDate == null) {
            throw new IllegalArgumentException("Stat date cannot be null");
        }
        
        List<HotKeywordDO> hotKeywordDOs = hotKeywordMapper.selectTopByDate(statDate, limit);
        
        return hotKeywordDOs.stream()
                .map(hotKeywordConvertor::toEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<HotKeyword> findByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        
        List<HotKeywordDO> hotKeywordDOs = hotKeywordMapper.selectByDateRange(startDate, endDate);
        
        return hotKeywordDOs.stream()
                .map(hotKeywordConvertor::toEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public void batchSave(List<HotKeyword> hotKeywords) {
        if (hotKeywords == null || hotKeywords.isEmpty()) {
            return;
        }
        
        for (HotKeyword hotKeyword : hotKeywords) {
            saveOrUpdate(hotKeyword);
        }
    }
}
