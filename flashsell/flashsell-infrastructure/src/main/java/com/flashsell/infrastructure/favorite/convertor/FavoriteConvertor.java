package com.flashsell.infrastructure.favorite.convertor;

import com.flashsell.domain.favorite.entity.Favorite;
import com.flashsell.infrastructure.favorite.dataobject.FavoriteDO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏转换器
 * 负责 FavoriteDO 和领域实体之间的转换
 */
@Component
public class FavoriteConvertor {

    /**
     * 将收藏数据对象转换为领域实体
     *
     * @param favoriteDO 收藏数据对象
     * @return 收藏领域实体
     */
    public Favorite toEntity(FavoriteDO favoriteDO) {
        if (favoriteDO == null) {
            return null;
        }

        return Favorite.builder()
                .id(favoriteDO.getId())
                .userId(favoriteDO.getUserId())
                .productId(favoriteDO.getProductId())
                .createdAt(favoriteDO.getCreatedAt())
                .build();
    }

    /**
     * 将收藏领域实体转换为数据对象
     *
     * @param favorite 收藏领域实体
     * @return 收藏数据对象
     */
    public FavoriteDO toDataObject(Favorite favorite) {
        if (favorite == null) {
            return null;
        }

        return FavoriteDO.builder()
                .id(favorite.getId())
                .userId(favorite.getUserId())
                .productId(favorite.getProductId())
                .createdAt(favorite.getCreatedAt())
                .build();
    }

    /**
     * 将收藏数据对象列表转换为领域实体列表
     *
     * @param favoriteDOList 收藏数据对象列表
     * @return 收藏领域实体列表
     */
    public List<Favorite> toEntityList(List<FavoriteDO> favoriteDOList) {
        if (favoriteDOList == null) {
            return List.of();
        }
        return favoriteDOList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
