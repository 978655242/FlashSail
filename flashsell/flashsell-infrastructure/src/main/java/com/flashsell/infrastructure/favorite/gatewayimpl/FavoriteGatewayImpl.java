package com.flashsell.infrastructure.favorite.gatewayimpl;

import com.flashsell.domain.favorite.entity.Favorite;
import com.flashsell.domain.favorite.gateway.FavoriteGateway;
import com.flashsell.infrastructure.favorite.convertor.FavoriteConvertor;
import com.flashsell.infrastructure.favorite.dataobject.FavoriteDO;
import com.flashsell.infrastructure.favorite.mapper.FavoriteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 收藏网关实现
 * 实现 FavoriteGateway 接口，提供收藏数据访问的具体实现
 */
@Repository
@RequiredArgsConstructor
public class FavoriteGatewayImpl implements FavoriteGateway {

    private final FavoriteMapper favoriteMapper;
    private final FavoriteConvertor favoriteConvertor;

    @Override
    public Optional<Favorite> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        FavoriteDO favoriteDO = favoriteMapper.selectById(id);
        return Optional.ofNullable(favoriteConvertor.toEntity(favoriteDO));
    }

    @Override
    public Optional<Favorite> findByUserIdAndProductId(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return Optional.empty();
        }
        FavoriteDO favoriteDO = favoriteMapper.selectByUserIdAndProductId(userId, productId);
        return Optional.ofNullable(favoriteConvertor.toEntity(favoriteDO));
    }

    @Override
    public List<Favorite> findByUserId(Long userId, int page, int pageSize) {
        if (userId == null || page < 1 || pageSize < 1) {
            return List.of();
        }
        int offset = (page - 1) * pageSize;
        List<FavoriteDO> favoriteDOList = favoriteMapper.selectByUserIdWithPagination(userId, offset, pageSize);
        return favoriteConvertor.toEntityList(favoriteDOList);
    }

    @Override
    public List<Favorite> findAllByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        List<FavoriteDO> favoriteDOList = favoriteMapper.selectAllByUserId(userId);
        return favoriteConvertor.toEntityList(favoriteDOList);
    }

    @Override
    public long countByUserId(Long userId) {
        if (userId == null) {
            return 0;
        }
        return favoriteMapper.countByUserId(userId);
    }

    @Override
    public boolean existsByUserIdAndProductId(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return false;
        }
        return favoriteMapper.countByUserIdAndProductId(userId, productId) > 0;
    }

    @Override
    public Favorite save(Favorite favorite) {
        if (favorite == null) {
            throw new IllegalArgumentException("Favorite cannot be null");
        }

        FavoriteDO favoriteDO = favoriteConvertor.toDataObject(favorite);

        if (favoriteDO.getId() == null) {
            // 新增收藏
            if (favoriteDO.getCreatedAt() == null) {
                favoriteDO.setCreatedAt(LocalDateTime.now());
            }
            favoriteMapper.insert(favoriteDO);
        } else {
            // 更新收藏（通常不需要更新收藏）
            favoriteMapper.updateById(favoriteDO);
        }

        // 返回包含生成ID的收藏实体
        return favoriteConvertor.toEntity(favoriteDO);
    }

    @Override
    public void deleteById(Long id) {
        if (id != null) {
            favoriteMapper.deleteById(id);
        }
    }

    @Override
    public boolean deleteByUserIdAndProductId(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return false;
        }
        int deleted = favoriteMapper.deleteByUserIdAndProductId(userId, productId);
        return deleted > 0;
    }

    @Override
    public List<Long> findFavoriteProductIds(Long userId, List<Long> productIds) {
        if (userId == null || productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return favoriteMapper.selectFavoriteProductIds(userId, productIds);
    }
}
