package com.flashsell.infrastructure.ai.convertor;

import com.flashsell.domain.ai.entity.HotProductScore;
import com.flashsell.infrastructure.ai.dataobject.HotProductDO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 爆品推荐转换器
 * 负责 HotProductScore 和 HotProductDO 之间的转换
 */
@Component
public class HotProductConvertor {

    /**
     * DO 转 Entity
     *
     * @param hotProductDO 数据对象
     * @return 领域实体
     */
    public HotProductScore toEntity(HotProductDO hotProductDO) {
        if (hotProductDO == null) {
            return null;
        }

        return HotProductScore.builder()
                .id(hotProductDO.getId())
                .productId(hotProductDO.getProductId())
                .categoryId(hotProductDO.getCategoryId())
                .hotScore(hotProductDO.getHotScore())
                .rankInCategory(hotProductDO.getRankInCategory())
                .recommendDate(hotProductDO.getRecommendDate())
                .createdAt(hotProductDO.getCreatedAt())
                .build();
    }

    /**
     * Entity 转 DO
     *
     * @param hotProductScore 领域实体
     * @return 数据对象
     */
    public HotProductDO toDO(HotProductScore hotProductScore) {
        if (hotProductScore == null) {
            return null;
        }

        return HotProductDO.builder()
                .id(hotProductScore.getId())
                .productId(hotProductScore.getProductId())
                .categoryId(hotProductScore.getCategoryId())
                .hotScore(hotProductScore.getHotScore())
                .rankInCategory(hotProductScore.getRankInCategory())
                .recommendDate(hotProductScore.getRecommendDate())
                .createdAt(hotProductScore.getCreatedAt() != null ? 
                        hotProductScore.getCreatedAt() : LocalDateTime.now())
                .build();
    }

    /**
     * DO 列表转 Entity 列表
     *
     * @param hotProductDOList 数据对象列表
     * @return 领域实体列表
     */
    public List<HotProductScore> toEntityList(List<HotProductDO> hotProductDOList) {
        if (hotProductDOList == null) {
            return null;
        }

        return hotProductDOList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * Entity 列表转 DO 列表
     *
     * @param hotProductScoreList 领域实体列表
     * @return 数据对象列表
     */
    public List<HotProductDO> toDOList(List<HotProductScore> hotProductScoreList) {
        if (hotProductScoreList == null) {
            return null;
        }

        return hotProductScoreList.stream()
                .map(this::toDO)
                .collect(Collectors.toList());
    }
}
