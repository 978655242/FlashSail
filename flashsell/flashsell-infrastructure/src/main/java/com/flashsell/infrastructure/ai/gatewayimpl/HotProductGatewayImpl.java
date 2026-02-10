package com.flashsell.infrastructure.ai.gatewayimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flashsell.domain.ai.entity.HotProductScore;
import com.flashsell.domain.ai.gateway.HotProductGateway;
import com.flashsell.infrastructure.ai.convertor.HotProductConvertor;
import com.flashsell.infrastructure.ai.dataobject.HotProductDO;
import com.flashsell.infrastructure.ai.mapper.HotProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 爆品推荐网关实现
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class HotProductGatewayImpl implements HotProductGateway {

    private final HotProductMapper hotProductMapper;
    private final HotProductConvertor hotProductConvertor;

    @Override
    public void save(HotProductScore hotProductScore) {
        HotProductDO hotProductDO = hotProductConvertor.toDO(hotProductScore);
        hotProductMapper.insert(hotProductDO);
        hotProductScore.setId(hotProductDO.getId());
    }

    @Override
    public void batchSave(List<HotProductScore> hotProductScores) {
        if (hotProductScores == null || hotProductScores.isEmpty()) {
            return;
        }

        for (HotProductScore score : hotProductScores) {
            save(score);
        }
    }

    @Override
    public List<HotProductScore> findByDateAndCategory(LocalDate date, Long categoryId) {
        List<HotProductDO> hotProductDOList = hotProductMapper.selectByDateAndCategory(date, categoryId);
        return hotProductConvertor.toEntityList(hotProductDOList);
    }

    @Override
    public List<HotProductScore> findByDate(LocalDate date) {
        List<HotProductDO> hotProductDOList = hotProductMapper.selectByDate(date);
        return hotProductConvertor.toEntityList(hotProductDOList);
    }

    @Override
    public List<HotProductScore> findProductHistory(Long productId, LocalDate startDate, LocalDate endDate) {
        List<HotProductDO> hotProductDOList = hotProductMapper.selectProductHistory(productId, startDate, endDate);
        return hotProductConvertor.toEntityList(hotProductDOList);
    }

    @Override
    public int deleteBeforeDate(LocalDate beforeDate) {
        return hotProductMapper.deleteBeforeDate(beforeDate);
    }

    @Override
    public List<HotProductScore> findTopNByDateAndCategory(LocalDate date, Long categoryId, int topN) {
        List<HotProductDO> hotProductDOList = hotProductMapper.selectTopNByDateAndCategory(date, categoryId, topN);
        return hotProductConvertor.toEntityList(hotProductDOList);
    }

    @Override
    public boolean existsByDateAndCategory(LocalDate date, Long categoryId) {
        int count = hotProductMapper.countByDateAndCategory(date, categoryId);
        return count > 0;
    }

    @Override
    public int deleteByDateAndCategory(LocalDate date, Long categoryId) {
        return hotProductMapper.deleteByDateAndCategory(date, categoryId);
    }
}
